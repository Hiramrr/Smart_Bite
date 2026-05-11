package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Categoria
import com.smart.comida.data.repository.CategoriaRepository
import kotlinx.coroutines.launch

class CategoriaViewModel : ViewModel() {
    private val repository = CategoriaRepository()

    var uiState by mutableStateOf<CategoriaUiState>(CategoriaUiState.Loading)
        private set

    var mensajeOperacion by mutableStateOf<String?>(null)
        private set

    fun cargarCategorias() {
        uiState = CategoriaUiState.Loading
        viewModelScope.launch {
            repository.obtenerCategorias().onSuccess { lista ->
                uiState = CategoriaUiState.Success(lista)
            }.onFailure { error ->
                uiState = CategoriaUiState.Error("No se pudieron cargar las categorías", error)
            }
        }
    }

    fun crearCategoria(nombre: String) {
        if (nombre.isBlank()) {
            mensajeOperacion = "El nombre de la categoría es obligatorio."
            return
        }

        viewModelScope.launch {
            repository.crearCategoria(nombre.trim()).onSuccess {
                mensajeOperacion = "Categoría creada correctamente."
                cargarCategorias()
            }.onFailure { error ->
                val causa = error.message ?: "Error desconocido"
                mensajeOperacion = "Error al crear: $causa"
            }
        }
    }

    fun actualizarCategoria(id: Int, nombre: String) {
        if (nombre.isBlank()) {
            mensajeOperacion = "El nombre de la categoría es obligatorio."
            return
        }

        viewModelScope.launch {
            repository.actualizarCategoria(id, nombre.trim()).onSuccess {
                mensajeOperacion = "Categoría actualizada correctamente."
                cargarCategorias()
            }.onFailure {
                mensajeOperacion = "Error al actualizar la categoría. Intenta nuevamente."
            }
        }
    }

    fun eliminarCategoria(id: Int) {
        viewModelScope.launch {
            repository.eliminarCategoria(id).onSuccess {
                mensajeOperacion = "Categoría eliminada."
                cargarCategorias()
            }.onFailure {
                mensajeOperacion = "Error al eliminar la categoría: ${it.message}"
            }
        }
    }

    fun limpiarMensajeOperacion() {
        mensajeOperacion = null
    }
}

sealed class CategoriaUiState {
    object Loading : CategoriaUiState()
    data class Success(val categorias: List<Categoria>) : CategoriaUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : CategoriaUiState()
}
