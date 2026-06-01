package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Categoria
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

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
    var imagenUrl by mutableStateOf<String?>(null) // Mantiene la URL de la foto actual

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
                imagenUrl = ing.imagenUrl // --- CARGAMOS LA IMAGEN VIEJA ---
            }.onFailure {
                uiState = IngredienteUiState.Error("No se pudo cargar la información del ingrediente", it)
            }
        }
    }

    // Enviar cambios
    fun guardarCambios(id: Int, imagenBytes: ByteArray? = null) { // Acepta la foto nueva
        val nombreTrimmed = nombre.trim()

        if (nombreTrimmed.isBlank() || cantidad.isBlank() || unidad.isBlank() ||
            categoriaSeleccionada == null) {
            uiState = IngredienteUiState.Error("Completa los campos obligatorios.")
            return
        }

        val cant = cantidad.toFloatOrNull()
        if (cant == null || cant < 0) {
            uiState = IngredienteUiState.Error("La cantidad debe ser un número válido mayor o igual a 0.")
            return
        }

        if (fechaCaducidad.isNotBlank()) {
            try {
                if (LocalDate.parse(fechaCaducidad).isBefore(LocalDate.now())) {
                    uiState = IngredienteUiState.Error("La fecha de caducidad debe ser mayor o igual a la de hoy.")
                    return
                }
            } catch (e: DateTimeParseException) {
                uiState = IngredienteUiState.Error("El formato de la fecha debe ser YYYY-MM-DD.")
                return
            }
        }

        uiState = IngredienteUiState.Loading

        viewModelScope.launch {
            var urlFinalParaGuardar = imagenUrl // Por defecto, conservamos la foto vieja

            // --- LÓGICA DE ACTUALIZACIÓN DE FOTO ---
            if (imagenBytes != null) {
                // 1. Subimos la nueva foto
                val nombreArchivo = "foto_${System.currentTimeMillis()}"
                val resultadoImagen = repository.subirImagen(imagenBytes, nombreArchivo)

                resultadoImagen.onSuccess { nuevaUrl ->
                    // 2. Si se subió bien, BORRAMOS la vieja (si existía)
                    if (!imagenUrl.isNullOrEmpty()) {
                        repository.eliminarImagen(imagenUrl!!)
                    }
                    // 3. Asignamos la nueva URL para guardarla en la base de datos
                    urlFinalParaGuardar = nuevaUrl
                }.onFailure {
                    uiState = IngredienteUiState.Error("No se pudo actualizar la imagen", it)
                    return@launch
                }
            }
            // ----------------------------------------

            val resultado = repository.actualizarIngrediente(
                id = id, nombre = nombreTrimmed, cantidad = cant, unidad = unidad,
                fechaCaducidad = fechaCaducidad.ifBlank { null },
                categoriaId = categoriaSeleccionada?.id,
                imagenUrl = urlFinalParaGuardar // Pasamos la URL final
            )

            resultado.onSuccess {
                uiState = IngredienteUiState.Success
            }.onFailure {
                uiState = IngredienteUiState.Error("No se pudieron guardar los cambios", it)
            }
        }
    }

    fun resetState() { uiState = IngredienteUiState.Idle }
}
