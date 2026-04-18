package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.Categoria
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class IngredienteViewModel : ViewModel() {
    private val repository = InventarioRepository()

    var uiState by mutableStateOf<IngredienteUiState>(IngredienteUiState.Idle)
        private set

    // Estado para guardar las categorías descargadas
    var categorias by mutableStateOf<List<Categoria>>(emptyList())
        private set

    fun cargarCategorias() {
        viewModelScope.launch {
            repository.obtenerCategorias().onSuccess { lista ->
                categorias = lista
            }
        }
    }

    fun guardarIngrediente(
        nombre: String,
        cantidadStr: String,
        unidad: String,
        fechaCaducidad: String,
        categoriaId: Int? // Aceptamos el ID de la categoría
    ) {
        // --- FA-01: 'Campos vacíos' ---
        if (nombre.isBlank() || cantidadStr.isBlank() || unidad.isBlank()) {
            uiState = IngredienteUiState.Error("Por favor, completa los campos obligatorios.")
            return
        }

        val cantidad = cantidadStr.toFloatOrNull()
        if (cantidad == null) {
            uiState = IngredienteUiState.Error("La cantidad debe ser un número válido.")
            return
        }

        uiState = IngredienteUiState.Loading

        viewModelScope.launch {
            // --- FA-02: 'Ingrediente duplicado' ---
            val yaExiste = repository.existeIngrediente(nombre)
            if (yaExiste) {
                uiState = IngredienteUiState.Error("El ingrediente '$nombre' ya está en tu despensa.")
                return@launch // Detenemos la ejecución aquí
            }

            // Si llegamos hasta aquí, guardamos en Supabase
            val resultado = repository.agregarIngrediente(
                nombre = nombre,
                cantidad = cantidad,
                unidad = unidad,
                fechaCaducidad = fechaCaducidad.ifBlank { null },
                categoriaId = categoriaId
            )

            resultado.onSuccess {
                uiState = IngredienteUiState.Success
            }.onFailure {
                // --- Ex-01: 'Error al guardar' ---
                uiState = IngredienteUiState.Error("Error al guardar: ${it.message}")
            }
        }
    }

    fun resetState() {
        uiState = IngredienteUiState.Idle
    }
}

sealed class IngredienteUiState {
    object Idle : IngredienteUiState()
    object Loading : IngredienteUiState()
    object Success : IngredienteUiState()
    data class Error(val message: String) : IngredienteUiState()
}