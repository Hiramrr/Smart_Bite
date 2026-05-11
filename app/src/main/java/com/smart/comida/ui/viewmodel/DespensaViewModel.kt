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

    // Resumen del Dashboard
    var resumen by mutableStateOf(ResumenDespensa(0, 0, 0))
        private set

    data class ResumenDespensa(val total: Int, val porVencer: Int, val bajosStock: Int)

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
    fun eliminarIngrediente(id: Int, imagenUrl: String?) {
        viewModelScope.launch {
            // --- NUEVO: BORRAR LA IMAGEN FÍSICA PRIMERO ---
            if (!imagenUrl.isNullOrEmpty()) {
                repository.eliminarImagen(imagenUrl)
            }
            // ----------------------------------------------

            // Luego, borramos el registro de la base de datos
            val resultado = repository.eliminarIngrediente(id)

            resultado.onSuccess {
                // Volvemos a descargar la lista para que la pantalla se actualice
                cargarIngredientes()
            }.onFailure {
                uiState = DespensaUiState.Error("Error al eliminar ingrediente", it)
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
        val nuevaCantidad = ingrediente.cantidad - 1f
        viewModelScope.launch {
            if (nuevaCantidad <= 0f) {
                repository.registrarComoDesperdicio(ingrediente, ingrediente.cantidad).onSuccess {
                    mensajeOperacion = "Usaste ${ingrediente.nombre}."
                    cargarIngredientes()
                }.onFailure {
                    mensajeOperacion = "Error: ${it.message}"
                }
            } else {
                repository.actualizarIngrediente(
                    id = ingrediente.id,
                    nombre = ingrediente.nombre,
                    cantidad = nuevaCantidad,
                    unidad = ingrediente.unidad,
                    fechaCaducidad = ingrediente.fechaCaducidad,
                    categoriaId = ingrediente.categoriaId,
                    imagenUrl = ingrediente.imagenUrl
                ).onSuccess {
                    mensajeOperacion = "Usaste 1 ${ingrediente.unidad ?: "unidad"} de ${ingrediente.nombre}."
                    cargarIngredientes()
                }.onFailure {
                    mensajeOperacion = "Error al descontar: ${it.message}"
                }
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
