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

        // Validar que sea un número válido y que NO sea negativo
        if (cant == null || cant < 0) {
            uiState = IngredienteUiState.Error("La cantidad debe ser un número válido mayor o igual a 0.")
            return
        }

        // Validar formato de fecha (YYYY-MM-DD) si el usuario ingresó una
        if (fechaCaducidad.isNotBlank()) {
            val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
            if (!fechaCaducidad.matches(dateRegex)) {
                uiState = IngredienteUiState.Error("El formato de la fecha debe ser YYYY-MM-DD.")
                return
            }
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
                // Falla de conexión o base de datos.
                // El estado cambia a Error, pero las variables (nombre, cantidad, etc.)
                // NO se borran, manteniendo intactos los datos del ingrediente.
                uiState = IngredienteUiState.Error("Error al guardar: Verifica tu conexión a internet o intenta más tarde.")

            }
        }
    }

    fun resetState() { uiState = IngredienteUiState.Idle }
}