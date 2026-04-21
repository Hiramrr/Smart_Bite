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

    // Lista de categorías para los filtros
    var categorias by mutableStateOf<List<Categoria>>(emptyList())
        private set

    // Guardamos la lista original para no tener que descargarla cada vez que filtramos
    private var todosLosIngredientes: List<Ingrediente> = emptyList()

    // Estado de los filtros
    var filtroSeleccionado by mutableStateOf<Categoria?>(null)
        private set
    var diasFiltroCaducidad by mutableStateOf<Int?>(null)
        private set

    init {
        // Al nacer el ViewModel, descargamos las categorías para los botones
        viewModelScope.launch {
            repository.obtenerCategorias().onSuccess { categorias = it }
        }
    }

    fun cargarIngredientes() {
        uiState = DespensaUiState.Loading
        viewModelScope.launch {
            repository.obtenerIngredientes().onSuccess { lista ->
                todosLosIngredientes = lista
                aplicarFiltros() // Mostramos la lista aplicando el filtro actual
            }.onFailure { error ->
                uiState = DespensaUiState.Error("Error al cargar: ${error.message}")
            }
        }
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
                uiState = DespensaUiState.Error("Error al eliminar: ${it.message}")
            }
        }
    }

    fun registrarComoDesperdicio(ingrediente: Ingrediente) {
        if (ingrediente.id == null) {
            mensajeOperacion = "No se pudo registrar el desperdicio: ingrediente inválido."
            return
        }

        viewModelScope.launch {
            val resultado = repository.registrarComoDesperdicio(ingrediente)
            resultado.onSuccess {
                mensajeOperacion = "Ingrediente registrado como desperdicio."
                cargarIngredientes()
            }.onFailure {
                mensajeOperacion = "Error al registrar desperdicio: ${it.message ?: "Intenta nuevamente."}"
            }
        }
    }

    fun limpiarMensajeOperacion() {
        mensajeOperacion = null
    }

    fun cargarHistorialDesperdicio() {
        historialUiState = HistorialDesperdicioUiState.Loading
        viewModelScope.launch {
            repository.obtenerHistorialDesperdicio()
                .onSuccess { historialUiState = HistorialDesperdicioUiState.Success(it) }
                .onFailure { historialUiState = HistorialDesperdicioUiState.Error("Error al cargar historial: ${it.message}") }
        }
    }

    // --- LÓGICA DE FILTROS ---

    fun seleccionarFiltroCategoria(categoria: Categoria?) {
        filtroSeleccionado = categoria
        diasFiltroCaducidad = null // Apagamos el de caducar si elegimos una categoría
        aplicarFiltros()
    }

    fun establecerFiltroCaducidad(dias: Int?) {
        diasFiltroCaducidad = dias
        if (dias != null) {
            filtroSeleccionado = null // Apagamos las categorías si elegimos por caducar
        }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var listaFiltrada = todosLosIngredientes

        if (filtroSeleccionado != null) {
            // Filtramos solo los que pertenezcan a la categoría seleccionada
            listaFiltrada = listaFiltrada.filter { it.categoriaId == filtroSeleccionado?.id }
        } else if (diasFiltroCaducidad != null) {
            // Filtramos ingredientes que caducan pronto (según los días elegidos) o ya caducaron
            val hoy = java.time.LocalDate.now()
            val limite = hoy.plusDays(diasFiltroCaducidad!!.toLong())
            
            listaFiltrada = listaFiltrada.filter { ingrediente ->
                ingrediente.fechaCaducidad?.let { fechaStr ->
                    try {
                        val fecha = java.time.LocalDate.parse(fechaStr)
                        !fecha.isAfter(limite)
                    } catch (e: Exception) {
                        false
                    }
                } ?: false
            }
        }

        uiState = DespensaUiState.Success(listaFiltrada)
    }
}

sealed class DespensaUiState {
    object Loading : DespensaUiState()
    data class Success(val ingredientes: List<Ingrediente>) : DespensaUiState()
    data class Error(val message: String) : DespensaUiState()
}

sealed class HistorialDesperdicioUiState {
    object Loading : HistorialDesperdicioUiState()
    data class Success(val desperdicios: List<Desperdicio>) : HistorialDesperdicioUiState()
    data class Error(val message: String) : HistorialDesperdicioUiState()
}
