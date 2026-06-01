package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Categoria
import com.smart.comida.data.model.Desperdicio
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class DespensaViewModel : ViewModel() {
    private val repository = InventarioRepository()

    var uiState by mutableStateOf<DespensaUiState>(DespensaUiState.Loading)
        private set

    var mensajeOperacion by mutableStateOf<String?>(null)
        private set

    var historialUiState by mutableStateOf<HistorialDesperdicioUiState>(HistorialDesperdicioUiState.Loading)
        private set

    var categorias by mutableStateOf<List<Categoria>>(emptyList())
        private set

    private var todosLosIngredientes: List<Ingrediente> = emptyList()

    // Estado de los filtros
    var filtroSeleccionado by mutableStateOf<Categoria?>(null)
        private set
    var diasFiltroCaducidad by mutableStateOf<Int?>(null)
        private set
    var searchQuery by mutableStateOf("")
        private set
    var ordenSeleccionado by mutableStateOf(OrdenDespensa.CADUCIDAD)
        private set

    // Resumen del Dashboard
    var resumen by mutableStateOf(ResumenDespensa(0, 0, 0))
        private set

    data class ResumenDespensa(val total: Int, val porVencer: Int, val bajosStock: Int)

    enum class OrdenDespensa {
        CADUCIDAD,
        NOMBRE,
        CATEGORIA,
        MENOR_STOCK
    }

    init {
        // Al nacer el ViewModel, descargamos las categorías para los botones
        viewModelScope.launch {
            repository.obtenerCategorias().onSuccess { categorias = it }
        }
    }

    fun cargarIngredientes() {
        uiState = DespensaUiState.Loading
        viewModelScope.launch {
            // Aprovechamos para cargar categorías también, por si falló en el init o hay nuevas
            repository.obtenerCategorias().onSuccess { categorias = it }

            repository.obtenerIngredientes().onSuccess { lista ->
                todosLosIngredientes = lista
                actualizarResumen()
                aplicarFiltros()
            }.onFailure { error ->
                uiState = DespensaUiState.Error("Error al cargar ingredientes", error)
            }
        }
    }

    private fun actualizarResumen() {
        val hoy = java.time.LocalDate.now()
        val fechaLimite = hoy.plusDays(7)

        val total = todosLosIngredientes.size
        val porVencer = todosLosIngredientes.count {
            if (it.fechaCaducidad.isNullOrEmpty()) false
            else {
                try {
                    val fecha = java.time.LocalDate.parse(it.fechaCaducidad)
                    !fecha.isBefore(hoy) && !fecha.isAfter(fechaLimite)
                } catch (e: Exception) { false }
            }
        }
        val bajosStock = todosLosIngredientes.count { it.cantidad <= 2f }

        resumen = ResumenDespensa(total, porVencer, bajosStock)
    }

    // Asegúrate de pedir la imagenUrl como parámetro
    fun eliminarIngrediente(id: Int, imagenUrl: String?, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val resultado = repository.eliminarIngrediente(id)

            resultado.onSuccess {
                if (!imagenUrl.isNullOrEmpty()) {
                    repository.eliminarImagen(imagenUrl)
                }
                // Volvemos a descargar la lista para que la pantalla se actualice
                cargarIngredientes()
                onResult(true)
            }.onFailure {
                mensajeOperacion = "No se pudo eliminar el ingrediente. Revisa tu conexión a internet e intenta nuevamente."
                onResult(false)
            }
        }
    }

    fun registrarComoDesperdicio(ingrediente: Ingrediente, cantidadDesperdicio: Float) {
        if (ingrediente.id == null) {
            mensajeOperacion = "No se pudo registrar el desperdicio: ingrediente inválido."
            return
        }

        viewModelScope.launch {
            val resultado = repository.registrarComoDesperdicio(ingrediente, cantidadDesperdicio)
            resultado.onSuccess {
                mensajeOperacion = "Desperdicio registrado correctamente."
                cargarIngredientes()
            }.onFailure {
                mensajeOperacion = "Error al registrar desperdicio: ${it.message ?: "Intenta nuevamente."}"
            }
        }
    }

    fun usarIngrediente(ingrediente: Ingrediente) {
        if (ingrediente.id == null) {
            mensajeOperacion = "No se pudo usar el ingrediente."
            return
        }
        viewModelScope.launch {
            repository.descontarIngrediente(ingrediente, 1f).onSuccess {
                mensajeOperacion = "Usaste 1 ${ingrediente.unidad ?: "unidad"} de ${ingrediente.nombre}."
                cargarIngredientes()
            }.onFailure {
                mensajeOperacion = "Error al descontar: ${it.message}"
            }
        }
    }

    fun limpiarMensajeOperacion() {
        mensajeOperacion = null
    }

    fun recargarCategorias() {
        viewModelScope.launch {
            repository.obtenerCategorias().onSuccess { categorias = it }
        }
    }

    fun cargarHistorialDesperdicio() {
        historialUiState = HistorialDesperdicioUiState.Loading
        viewModelScope.launch {
            repository.obtenerHistorialDesperdicio()
                .onSuccess { historialUiState = HistorialDesperdicioUiState.Success(it) }
                .onFailure { historialUiState = HistorialDesperdicioUiState.Error("Error al cargar historial", it) }
        }
    }


    fun actualizarBusqueda(query: String) {
        searchQuery = query
        aplicarFiltros()
    }

    fun seleccionarFiltroCategoria(categoria: Categoria?) {
        filtroSeleccionado = categoria
        diasFiltroCaducidad = null
        aplicarFiltros()
    }

    fun seleccionarFiltroCaducidad(dias: Int?) {
        if (diasFiltroCaducidad == dias) {
            diasFiltroCaducidad = null
        } else {
            diasFiltroCaducidad = dias
            if (dias != null) {
                filtroSeleccionado = null
            }
        }
        aplicarFiltros()
    }

    fun seleccionarOrden(orden: OrdenDespensa) {
        ordenSeleccionado = orden
        aplicarFiltros()
    }

    fun limpiarFiltros() {
        filtroSeleccionado = null
        diasFiltroCaducidad = null
        searchQuery = ""
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var listaFiltrada = todosLosIngredientes

        if (searchQuery.isNotBlank()) {
            listaFiltrada = listaFiltrada.filter {
                it.nombre.contains(searchQuery, ignoreCase = true)
            }
        }

        if (filtroSeleccionado != null) {
            // Filtramos solo los que pertenezcan a la categoría seleccionada
            listaFiltrada = listaFiltrada.filter { it.categoriaId == filtroSeleccionado?.id }
        } else if (diasFiltroCaducidad != null) {
            val hoy = java.time.LocalDate.now()
            val fechaLimite = hoy.plusDays(diasFiltroCaducidad!!.toLong())
            listaFiltrada = listaFiltrada.filter {
                if (it.fechaCaducidad.isNullOrEmpty()) {
                    false
                } else {
                    try {
                        val fechaIngrediente = java.time.LocalDate.parse(it.fechaCaducidad)
                        !fechaIngrediente.isBefore(hoy) && !fechaIngrediente.isAfter(fechaLimite)
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        }

        listaFiltrada = when (ordenSeleccionado) {
            OrdenDespensa.CADUCIDAD -> listaFiltrada.sortedWith(
                compareBy<Ingrediente> {
                    it.fechaCaducidad?.let { fecha ->
                        runCatching { java.time.LocalDate.parse(fecha) }.getOrNull()
                    } ?: java.time.LocalDate.MAX
                }.thenBy { it.nombre.lowercase() }
            )
            OrdenDespensa.NOMBRE -> listaFiltrada.sortedBy { it.nombre.lowercase() }
            OrdenDespensa.CATEGORIA -> {
                val categoriasPorId = categorias.associate { it.id to it.nombre.lowercase() }
                listaFiltrada.sortedWith(
                    compareBy<Ingrediente> { categoriasPorId[it.categoriaId] ?: "zzzz" }
                        .thenBy { it.nombre.lowercase() }
                )
            }
            OrdenDespensa.MENOR_STOCK -> listaFiltrada.sortedWith(
                compareBy<Ingrediente> { it.cantidad }.thenBy { it.nombre.lowercase() }
            )
        }

        uiState = DespensaUiState.Success(listaFiltrada)
    }
}

sealed class DespensaUiState {
    object Loading : DespensaUiState()
    data class Success(val ingredientes: List<Ingrediente>) : DespensaUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : DespensaUiState()
}

sealed class HistorialDesperdicioUiState {
    object Loading : HistorialDesperdicioUiState()
    data class Success(val desperdicios: List<Desperdicio>) : HistorialDesperdicioUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : HistorialDesperdicioUiState()
}
