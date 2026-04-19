package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Categoria
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class DespensaViewModel : ViewModel() {
    private val repository = InventarioRepository()

    var uiState by mutableStateOf<DespensaUiState>(DespensaUiState.Loading)
        private set

    // Lista de categorías para los filtros
    var categorias by mutableStateOf<List<Categoria>>(emptyList())
        private set

    // Guardamos la lista original para no tener que descargarla cada vez que filtramos
    private var todosLosIngredientes: List<Ingrediente> = emptyList()

    // Estado de los filtros
    var filtroSeleccionado by mutableStateOf<Categoria?>(null)
        private set
    var filtroPorCaducar by mutableStateOf(false)
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

    // --- LÓGICA DE FILTROS ---

    fun seleccionarFiltroCategoria(categoria: Categoria?) {
        filtroSeleccionado = categoria
        filtroPorCaducar = false // Apagamos el de caducar si elegimos una categoría
        aplicarFiltros()
    }

    fun toggleFiltroPorCaducar() {
        filtroPorCaducar = !filtroPorCaducar
        if (filtroPorCaducar) {
            filtroSeleccionado = null // Apagamos las categorías si elegimos por caducar
        }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var listaFiltrada = todosLosIngredientes

        if (filtroSeleccionado != null) {
            // Filtramos solo los que pertenezcan a la categoría seleccionada
            listaFiltrada = listaFiltrada.filter { it.categoriaId == filtroSeleccionado?.id }
        } else if (filtroPorCaducar) {
            // Lógica pendiente (por ahora mostrará todos, lo implementaremos después)
        }

        uiState = DespensaUiState.Success(listaFiltrada)
    }
}

sealed class DespensaUiState {
    object Loading : DespensaUiState()
    data class Success(val ingredientes: List<Ingrediente>) : DespensaUiState()
    data class Error(val message: String) : DespensaUiState()
}