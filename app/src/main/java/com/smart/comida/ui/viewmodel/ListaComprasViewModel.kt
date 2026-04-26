package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.ArticuloCompra
import com.smart.comida.data.repository.ListaComprasRepository
import kotlinx.coroutines.launch

class ListaComprasViewModel : ViewModel() {
    private val repository = ListaComprasRepository()

    var uiState by mutableStateOf<ListaComprasUiState>(ListaComprasUiState.Loading)
        private set

    var mensajeOperacion by mutableStateOf<String?>(null)
        private set

    fun cargarArticulos() {
        uiState = ListaComprasUiState.Loading
        viewModelScope.launch {
            repository.obtenerArticulos().onSuccess { lista ->
                uiState = ListaComprasUiState.Success(lista)
            }.onFailure { error ->
                uiState = ListaComprasUiState.Error("Error al cargar: ${error.message}")
            }
        }
    }

    fun agregarArticulo(nombre: String, cantidadEsperada: String) {
        if (nombre.isBlank()) {
            mensajeOperacion = "El nombre del producto no puede estar vacío."
            return
        }
        viewModelScope.launch {
            val resultado = repository.agregarArticulo(nombre, cantidadEsperada)
            resultado.onSuccess {
                mensajeOperacion = "Artículo agregado exitosamente."
                cargarArticulos()
            }.onFailure { error ->
                mensajeOperacion = "Error al agregar el artículo: ${error.message ?: "Intenta nuevamente."}"
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

    fun limpiarMensajeOperacion() {
        mensajeOperacion = null
    }
}

sealed class ListaComprasUiState {
    object Loading : ListaComprasUiState()
    data class Success(val articulos: List<ArticuloCompra>) : ListaComprasUiState()
    data class Error(val message: String) : ListaComprasUiState()
}
