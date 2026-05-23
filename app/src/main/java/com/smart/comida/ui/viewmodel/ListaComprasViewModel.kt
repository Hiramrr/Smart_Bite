package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbite.data.Ingredient
import com.smart.comida.data.model.ArticuloCompra
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.repository.InventarioRepository
import com.smart.comida.data.repository.ListaComprasRepository
import com.smart.comida.data.repository.OpenFoodFactsRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.text.Normalizer

data class IngredienteFaltante(
    val nombre: String,
    val cantidad: Double?,
    val unidad: String?
)

sealed class AgregarDesdeRecetaState {
    object Idle : AgregarDesdeRecetaState()
    object Comparing : AgregarDesdeRecetaState()
    data class ShowConfirmation(val ingredientesFaltantes: List<IngredienteFaltante>) : AgregarDesdeRecetaState()
    data class AllAvailable(val message: String) : AgregarDesdeRecetaState()
    object Adding : AgregarDesdeRecetaState()
    data class Success(val message: String) : AgregarDesdeRecetaState()
    data class Error(val message: String) : AgregarDesdeRecetaState()
}

class ListaComprasViewModel : ViewModel() {
    private val repository = ListaComprasRepository()
    private val inventarioRepository = InventarioRepository()
    private val openFoodFactsRepository = OpenFoodFactsRepository()

    var uiState by mutableStateOf<ListaComprasUiState>(ListaComprasUiState.Loading)
        private set

    var agregarDesdeRecetaState by mutableStateOf<AgregarDesdeRecetaState>(AgregarDesdeRecetaState.Idle)
        private set

    var mensajeOperacion by mutableStateOf<String?>(null)
        private set

    fun cargarArticulos() {
        uiState = ListaComprasUiState.Loading
        viewModelScope.launch {
            repository.obtenerArticulos().onSuccess { lista ->
                uiState = ListaComprasUiState.Success(lista)
            }.onFailure { error ->
                uiState = ListaComprasUiState.Error("No se pudo cargar la lista de compras", error)
            }
        }
    }

    fun editarArticulo(id: Int, nombre: String, cantidadInput: String, unidad: String?) {
        val nombreTrimmed = nombre.trim()
        if (nombreTrimmed.isBlank()) {
            mensajeOperacion = "El nombre del producto es obligatorio."
            return
        }

        val cantidad = cantidadInput.replace(',', '.').toDoubleOrNull()
        if (cantidadInput.isNotBlank() && (cantidad == null || cantidad <= 0)) {
            mensajeOperacion = "Por favor, ingresa una cantidad válida mayor a cero."
            return
        }

        viewModelScope.launch {
            val resultado = repository.actualizarArticulo(id, nombreTrimmed, cantidad, unidad)
            resultado.onSuccess {
                mensajeOperacion = "Producto actualizado correctamente."
                cargarArticulos()
            }.onFailure { error ->
                mensajeOperacion = "No se pudo actualizar el producto. Verifica tu conexión."
            }
        }
    }

    fun agregarArticulo(nombre: String, cantidadInput: String, unidad: String?) {
        val nombreTrimmed = nombre.trim()
        if (nombreTrimmed.isBlank()) {
            mensajeOperacion = "El nombre del producto es obligatorio."
            return
        }

        val cantidad = cantidadInput.replace(',', '.').toDoubleOrNull()
        if (cantidadInput.isBlank()) {
            mensajeOperacion = "Por favor, ingresa la cantidad que deseas comprar."
            return
        }
        
        if (cantidad == null || cantidad <= 0) {
            mensajeOperacion = "La cantidad debe ser un número válido mayor a cero."
            return
        }

        viewModelScope.launch {
            val resultado = repository.agregarArticulo(nombreTrimmed, cantidad, unidad)
            resultado.onSuccess {
                mensajeOperacion = "Producto añadido a la lista."
                cargarArticulos()
            }.onFailure { error ->
                mensajeOperacion = "Error al guardar el producto. Intenta nuevamente."
            }
        }
    }

    fun eliminarArticulo(id: Int) {
        viewModelScope.launch {
            repository.eliminarArticulo(id).onSuccess {
                cargarArticulos()
            }.onFailure {
                mensajeOperacion = "Error al eliminar: ${it.message}"
            }
        }
    }

    fun marcarComoComprado(id: Int, estadoActual: String?) {
        val nuevoEstado = if (estadoActual == "Comprado") "Pendiente" else "Comprado"
        viewModelScope.launch {
            repository.actualizarEstado(id, nuevoEstado).onSuccess {
                cargarArticulos()
            }.onFailure {
                mensajeOperacion = "No se pudo actualizar el artículo, revisa tu conexion a internet"
            }
        }
    }

    fun confirmarCompra() {
        val estadoActual = uiState
        if (estadoActual !is ListaComprasUiState.Success) return
        val comprados = estadoActual.articulos.filter { it.estado == "Comprado" }
        if (comprados.isEmpty()) return

        viewModelScope.launch {
            var errorOcurrido = false
            var despensaErrorCount = 0

            for (articulo in comprados) {
                articulo.id?.let { id ->
                    repository.actualizarEstado(id, "Confirmado").onFailure {
                        errorOcurrido = true
                    }
                }

                val resultado = inventarioRepository.agregarIngrediente(
                    nombre = articulo.nombre,
                    cantidad = articulo.cantidadEsperada?.toFloat() ?: 1f,
                    unidad = articulo.unidad,
                    fechaCaducidad = null,
                    categoriaId = null,
                    imagenUrl = null
                )

                resultado.onSuccess { ingredienteId ->
                    viewModelScope.launch(Dispatchers.IO) {
                        openFoodFactsRepository.buscarProductoPorNombre(articulo.nombre).onSuccess { producto ->
                            producto?.imagenUrl?.let { url ->
                                inventarioRepository.actualizarImagenIngrediente(ingredienteId, url)
                            }
                        }
                    }
                }.onFailure {
                    despensaErrorCount++
                }
            }

            when {
                errorOcurrido -> mensajeOperacion = "Error al confirmar algunas compras."
                despensaErrorCount > 0 -> mensajeOperacion = "Compra confirmada, pero $despensaErrorCount producto(s) no se pudieron mover a la despensa."
                else -> mensajeOperacion = "Compra confirmada y productos movidos a la despensa."
            }
            cargarArticulos()
        }
    }

    fun limpiarMensajeOperacion() {
        mensajeOperacion = null
    }

    fun compararIngredientesConReceta(ingredientesReceta: List<Ingredient>) {
        agregarDesdeRecetaState = AgregarDesdeRecetaState.Comparing
        viewModelScope.launch {
            try {
                val inventarioResult = inventarioRepository.obtenerIngredientes()
                inventarioResult.onSuccess { inventario ->
                    val faltantes = identificarFaltantes(ingredientesReceta, inventario)
                    if (faltantes.isEmpty()) {
                        agregarDesdeRecetaState = AgregarDesdeRecetaState.AllAvailable(
                            "Ya tienes todos los ingredientes necesarios para preparar esta receta."
                        )
                    } else {
                        agregarDesdeRecetaState = AgregarDesdeRecetaState.ShowConfirmation(faltantes)
                    }
                }.onFailure {
                    agregarDesdeRecetaState = AgregarDesdeRecetaState.Error(
                        "No se pudo acceder al inventario. Verifica tu conexión."
                    )
                }
            } catch (e: Exception) {
                agregarDesdeRecetaState = AgregarDesdeRecetaState.Error(
                    "Error al comparar ingredientes: ${e.message}"
                )
            }
        }
    }

    private fun identificarFaltantes(
        ingredientesReceta: List<Ingredient>,
        inventario: List<Ingrediente>
    ): List<IngredienteFaltante> {
        val faltantes = mutableListOf<IngredienteFaltante>()

        for (ingredienteReceta in ingredientesReceta) {
            val (nombreLimpioOriginal, cantidadExtraidaOriginal) = extraerCantidadDeOriginal(ingredienteReceta.original)
            val nombreReceta = ingredienteReceta.name?.takeIf { it.isNotBlank() } ?: nombreLimpioOriginal
            val cantidadReceta = ingredienteReceta.amount
            val unidadReceta = ingredienteReceta.unit?.lowercase()?.trim()?.takeIf { it.isNotBlank() }

            if (nombreReceta.isBlank()) {
                faltantes.add(
                    IngredienteFaltante(
                        nombre = ingredienteReceta.original,
                        cantidad = cantidadExtraidaOriginal ?: cantidadReceta,
                        unidad = unidadReceta
                    )
                )
                continue
            }

            val ingredientesCoincidentes = inventario.filter { ing ->
                nombresIngredientesCoinciden(ing.nombre, nombreReceta)
            }

            if (ingredientesCoincidentes.isEmpty()) {
                faltantes.add(
                    IngredienteFaltante(
                        nombre = nombreLimpioOriginal.ifBlank { ingredienteReceta.name ?: ingredienteReceta.original },
                        cantidad = cantidadExtraidaOriginal ?: cantidadReceta,
                        unidad = unidadReceta
                    )
                )
            } else if (cantidadReceta != null && unidadReceta != null) {
                val coincidenciasMismaUnidad = ingredientesCoincidentes.filter {
                    it.unidad?.lowercase()?.trim() == unidadReceta
                }

                if (coincidenciasMismaUnidad.isEmpty()) {
                    continue
                }

                val cantidadTotalEnDespensa = coincidenciasMismaUnidad.sumOf { it.cantidad.toDouble() }

                if (cantidadTotalEnDespensa < cantidadReceta) {
                    faltantes.add(
                        IngredienteFaltante(
                            nombre = nombreLimpioOriginal.ifBlank { ingredienteReceta.name ?: ingredienteReceta.original },
                            cantidad = cantidadReceta - cantidadTotalEnDespensa,
                            unidad = unidadReceta
                        )
                    )
                }
            } else if (cantidadReceta == null) {
                continue
            } else {
                val cantidadTotalEnDespensa = ingredientesCoincidentes.sumOf { it.cantidad.toDouble() }
                if (cantidadTotalEnDespensa < cantidadReceta) {
                    faltantes.add(
                        IngredienteFaltante(
                            nombre = nombreLimpioOriginal.ifBlank { ingredienteReceta.name ?: ingredienteReceta.original },
                            cantidad = cantidadReceta - cantidadTotalEnDespensa,
                            unidad = unidadReceta
                        )
                    )
                }
            }
        }

        return faltantes
    }

    private fun nombresIngredientesCoinciden(nombreInventario: String, nombreReceta: String): Boolean {
        val inventarioNormalizado = normalizarNombreIngrediente(nombreInventario)
        val recetaNormalizada = normalizarNombreIngrediente(nombreReceta)

        if (inventarioNormalizado.isBlank() || recetaNormalizada.isBlank()) return false
        if (inventarioNormalizado == recetaNormalizada) return true

        val tokensInventario = inventarioNormalizado.split(" ").filter { it.length > 2 }
        val tokensReceta = recetaNormalizada.split(" ").filter { it.length > 2 }

        return tokensInventario.isNotEmpty() &&
            tokensReceta.isNotEmpty() &&
            (tokensContienenTodos(tokensInventario, tokensReceta) ||
                tokensContienenTodos(tokensReceta, tokensInventario))
    }

    private fun normalizarNombreIngrediente(nombre: String): String {
        val sinAcentos = Normalizer.normalize(nombre.lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")

        return sinAcentos
            .replace(Regex("[^a-z0-9ñ\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    private fun tokensContienenTodos(tokensBase: List<String>, tokensBuscados: List<String>): Boolean {
        return tokensBuscados.all { buscado ->
            tokensBase.any { base -> variantesPluralSingular(base).intersect(variantesPluralSingular(buscado)).isNotEmpty() }
        }
    }

    private fun variantesPluralSingular(palabra: String): Set<String> {
        return buildSet {
            add(palabra)
            if (palabra.length > 3 && palabra.endsWith("s")) add(palabra.dropLast(1))
            if (palabra.length > 4 && palabra.endsWith("es")) add(palabra.dropLast(2))
            if (palabra.length > 4 && palabra.endsWith("ces")) add("${palabra.dropLast(3)}z")
        }
    }

    private fun extraerCantidadDeOriginal(original: String): Pair<String, Double?> {
        val regex = Regex("^([\\d/]+(?:\\.[\\d]+)?)\\s*(.*)$")
        val matchResult = regex.find(original.trim())

        if (matchResult != null) {
            val cantidadStr = matchResult.groupValues[1]
            val resto = matchResult.groupValues[2].trim()
            val cantidad = parsearCantidad(cantidadStr)
            return Pair(resto, cantidad)
        }

        return Pair(original, null)
    }

    private fun parsearCantidad(cantidadStr: String): Double? {
        if (cantidadStr.contains("/")) {
            val partes = cantidadStr.split("/")
            if (partes.size == 2) {
                val numerador = partes[0].toDoubleOrNull()
                val denominador = partes[1].toDoubleOrNull()
                if (denominador != null && denominador != 0.0) {
                    return (numerador ?: 1.0) / denominador
                }
            }
        }
        return cantidadStr.toDoubleOrNull()
    }

    fun agregarFaltantesALista(ingredientesFaltantes: List<IngredienteFaltante>) {
        agregarDesdeRecetaState = AgregarDesdeRecetaState.Adding
        viewModelScope.launch {
            try {
                val articulos = ingredientesFaltantes.map { faltante ->
                    ArticuloCompra(
                        nombre = faltante.nombre,
                        cantidadEsperada = faltante.cantidad ?: 1.0,
                        unidad = faltante.unidad,
                        estado = "Pendiente"
                    )
                }

                val resultado = repository.agregarArticulos(articulos)
                resultado.onSuccess {
                    agregarDesdeRecetaState = AgregarDesdeRecetaState.Success(
                        "${ingredientesFaltantes.size} ingrediente(s) agregado(s) a la lista de compras."
                    )
                    cargarArticulos()
                }.onFailure {
                    agregarDesdeRecetaState = AgregarDesdeRecetaState.Error(
                        "Error al actualizar la lista de compras: ${it.message}"
                    )
                }
            } catch (e: Exception) {
                agregarDesdeRecetaState = AgregarDesdeRecetaState.Error(
                    "Error al actualizar la lista de compras: ${e.message}"
                )
            }
        }
    }

    fun resetearEstadoReceta() {
        agregarDesdeRecetaState = AgregarDesdeRecetaState.Idle
    }
}

sealed class ListaComprasUiState {
    object Loading : ListaComprasUiState()
    data class Success(val articulos: List<ArticuloCompra>) : ListaComprasUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : ListaComprasUiState()
}
