package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class RegistrarDesperdicioViewModel : ViewModel() {
    private val repository = InventarioRepository()

    var ingrediente by mutableStateOf<Ingrediente?>(null)
        private set

    var uiState by mutableStateOf<RegistrarDesperdicioUiState>(RegistrarDesperdicioUiState.Idle)
        private set

    fun cargarIngrediente(id: Int) {
        uiState = RegistrarDesperdicioUiState.Loading
        viewModelScope.launch {
            repository.obtenerIngredientePorId(id).onSuccess {
                ingrediente = it
                uiState = RegistrarDesperdicioUiState.Idle
            }.onFailure {
                uiState = RegistrarDesperdicioUiState.Error("No se pudo cargar el ingrediente", it)
            }
        }
    }

    fun registrarDesperdicio() {
        val ing = ingrediente ?: return

        uiState = RegistrarDesperdicioUiState.Loading
        viewModelScope.launch {
            repository.registrarComoDesperdicio(ing, ing.cantidad).onSuccess {
                uiState = RegistrarDesperdicioUiState.Success
            }.onFailure {
                uiState = RegistrarDesperdicioUiState.Error(
                    it.message ?: "No se pudo registrar el desperdicio",
                    it
                )
            }
        }
    }

    fun resetState() {
        uiState = RegistrarDesperdicioUiState.Idle
    }
}

sealed class RegistrarDesperdicioUiState {
    object Idle : RegistrarDesperdicioUiState()
    object Loading : RegistrarDesperdicioUiState()
    object Success : RegistrarDesperdicioUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : RegistrarDesperdicioUiState()
}
