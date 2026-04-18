package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Categoria
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class EditarIngredienteViewModel : ViewModel() {
    private val repository = InventarioRepository()

    // Usamos el mismo estado que ya tenías creado en IngredienteViewModel
    var uiState by mutableStateOf<IngredienteUiState>(IngredienteUiState.Idle)
        private set
    var categorias by mutableStateOf<List<Categoria>>(emptyList())
        private set

    // Variables del formulario
    var nombre by mutableStateOf("")
    var cantidad by mutableStateOf("")
    var unidad by mutableStateOf("")
    var fechaCaducidad by mutableStateOf("")
    var categoriaSeleccionada by mutableStateOf<Categoria?>(null)

    // Cargar datos al entrar a la pantalla
    fun cargarDatos(id: Int) {
        uiState = IngredienteUiState.Loading
        viewModelScope.launch {
            repository.obtenerCategorias().onSuccess { categorias = it }

            repository.obtenerIngredientePorId(id).onSuccess { ing ->
                nombre = ing.nombre
                cantidad = ing.cantidad.toString()
                unidad = ing.unidad ?: ""
                fechaCaducidad = ing.fechaCaducidad ?: ""
                categoriaSeleccionada = categorias.find { it.id == ing.categoriaId }
                uiState = IngredienteUiState.Idle
            }.onFailure {
                uiState = IngredienteUiState.Error("Error al cargar ingrediente")
            }
        }
    }

    // Enviar cambios
    fun guardarCambios(id: Int) {
        if (nombre.isBlank() || cantidad.isBlank() || unidad.isBlank()) {
            uiState = IngredienteUiState.Error("Completa los campos obligatorios.")
            return
        }

        val cant = cantidad.toFloatOrNull()
        if (cant == null) {
            uiState = IngredienteUiState.Error("La cantidad debe ser un número válido.")
            return
        }

        uiState = IngredienteUiState.Loading

        viewModelScope.launch {
            val resultado = repository.actualizarIngrediente(
                id = id, nombre = nombre, cantidad = cant, unidad = unidad,
                fechaCaducidad = fechaCaducidad.ifBlank { null },
                categoriaId = categoriaSeleccionada?.id
            )

            resultado.onSuccess {
                uiState = IngredienteUiState.Success
            }.onFailure {
                uiState = IngredienteUiState.Error("Error al actualizar: ${it.message}")
            }
        }
    }

    fun resetState() { uiState = IngredienteUiState.Idle }
}