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
                mensajeOperacion = "Error al actualizar: ${it.message}"
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
            for (articulo in comprados) {
                articulo.id?.let { id ->
                    repository.actualizarEstado(id, "Confirmado").onFailure {
                        errorOcurrido = true
                    }
                }
            }
            if (errorOcurrido) {
                mensajeOperacion = "Error al confirmar algunas compras."
            } else {
                mensajeOperacion = "Compra confirmada."
            }
            cargarArticulos()
        }
    }

    fun limpiarMensajeOperacion() {
        mensajeOperacion = null
    }
}

sealed class ListaComprasUiState {
    object Loading : ListaComprasUiState()
    data class Success(val articulos: List<ArticuloCompra>) : ListaComprasUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : ListaComprasUiState()
}
