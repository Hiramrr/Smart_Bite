package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class DespensaViewModel : ViewModel() {
    private val repository = InventarioRepository()

    // El estado inicial será "Cargando" mientras pedimos los datos a Supabase
    var uiState by mutableStateOf<DespensaUiState>(DespensaUiState.Loading)
        private set



    fun cargarIngredientes() {
        uiState = DespensaUiState.Loading
        viewModelScope.launch {
            val resultado = repository.obtenerIngredientes()

            resultado.onSuccess { lista ->
                uiState = DespensaUiState.Success(lista)
            }.onFailure { error ->
                uiState = DespensaUiState.Error("Error al cargar: ${error.message}")
            }
        }
    }

    // Llama al repositorio para borrar y recarga la lista
    fun eliminarIngrediente(id: Int) {
        viewModelScope.launch {
            repository.eliminarIngrediente(id).onSuccess {

                cargarIngredientes()
            }
        }
    }
}

// Representa los estados de la pantalla de lista
sealed class DespensaUiState {
    object Loading : DespensaUiState()
    data class Success(val ingredientes: List<Ingrediente>) : DespensaUiState()
    data class Error(val message: String) : DespensaUiState()
}