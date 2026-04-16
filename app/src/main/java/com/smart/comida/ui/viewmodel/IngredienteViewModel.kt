package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IngredienteViewModel : ViewModel() {
    private val repository = InventarioRepository()

    // Estado para manejar mensajes de error o éxito en la UI
    var uiState by mutableStateOf<IngredienteUiState>(IngredienteUiState.Idle)
        private set

    fun guardarIngrediente(
        nombre: String,
        cantidadStr: String,
        unidad: String,
        fechaCaducidad: String
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
            val resultado = repository.agregarIngrediente(
                nombre = nombre,
                cantidad = cantidad,
                unidad = unidad,
                fechaCaducidad = fechaCaducidad.ifBlank { null },
                categoriaId = null // De momento opcional
            )

            resultado.onSuccess {
                uiState = IngredienteUiState.Success
            }.onFailure {
                // --- Ex-01: 'Error al guardar los datos' ---
                uiState = IngredienteUiState.Error("Error al conectar con Supabase: ${it.message}")
            }
        }
    }

    fun resetState() {
        uiState = IngredienteUiState.Idle
    }
}

// Representa los diferentes estados de la pantalla de agregar ingrediente
sealed class IngredienteUiState {
    object Idle : IngredienteUiState()
    object Loading : IngredienteUiState()
    object Success : IngredienteUiState()
    data class Error(val message: String) : IngredienteUiState()
}