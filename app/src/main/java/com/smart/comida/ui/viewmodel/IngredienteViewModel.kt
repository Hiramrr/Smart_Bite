package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Categoria
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        categoriaId: Int?,
        imagenBytes: ByteArray? = null // --- NUEVO PARÁMETRO PARA LA FOTO ---
    ) {
        // --- FA-01: 'Campos vacíos' ---
        if (nombre.isBlank() || cantidadStr.isBlank() || unidad.isBlank()) {
            uiState = IngredienteUiState.Error("Por favor, completa los campos obligatorios.")
            return
        }

        val cantidad = cantidadStr.toFloatOrNull()
        if (cantidad == null || cantidad < 0) {
            uiState = IngredienteUiState.Error("La cantidad debe ser un número válido mayor o igual a 0.")
            return
        }

        // --- VALIDACIÓN DE FECHA ---
        if (fechaCaducidad.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaSeleccionada = sdf.parse(fechaCaducidad)
                val hoyStr = sdf.format(Date())
                val fechaHoy = sdf.parse(hoyStr)

                if (fechaSeleccionada != null && fechaHoy != null && fechaSeleccionada.before(fechaHoy)) {
                    uiState = IngredienteUiState.Error("La fecha de caducidad debe ser mayor o igual a la de hoy.")
                    return
                }
            } catch (e: Exception) {
                uiState = IngredienteUiState.Error("Formato de fecha inválido.")
                return
            }
        }

        uiState = IngredienteUiState.Loading

        viewModelScope.launch {
            // --- FA-02: 'Ingrediente duplicado' ---
            val yaExiste = repository.existeIngrediente(nombre)
            if (yaExiste) {
                uiState = IngredienteUiState.Error("El ingrediente '$nombre' ya está en tu despensa.")
                return@launch
            }

            // --- NUEVA LÓGICA: SUBIR IMAGEN PRIMERO ---
            var urlImagenSubida: String? = null

            if (imagenBytes != null) {
                // Le damos un nombre único basado en el tiempo exacto
                val nombreArchivo = "foto_${System.currentTimeMillis()}"
                val resultadoImagen = repository.subirImagen(imagenBytes, nombreArchivo)

                resultadoImagen.onSuccess { url ->
                    urlImagenSubida = url // Guardamos el link mágico de Supabase
                }.onFailure {
                    uiState = IngredienteUiState.Error("Error al subir la imagen: ${it.message}")
                    return@launch // Si falla la foto, detenemos el guardado
                }
            }
            // ------------------------------------------

            // Guardamos en Supabase incluyendo la URL de la imagen (si hay una)
            val resultado = repository.agregarIngrediente(
                nombre = nombre,
                cantidad = cantidad,
                unidad = unidad,
                fechaCaducidad = fechaCaducidad.ifBlank { null },
                categoriaId = categoriaId,
                imagenUrl = urlImagenSubida // --- PASAMOS LA URL ---
            )

            resultado.onSuccess {
                uiState = IngredienteUiState.Success
            }.onFailure {
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