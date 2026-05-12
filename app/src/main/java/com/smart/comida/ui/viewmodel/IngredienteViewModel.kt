package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Categoria
import com.smart.comida.data.repository.InventarioRepository
import com.smart.comida.data.repository.OpenFoodFactsRepository
import com.smart.comida.data.repository.ProductDetails
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IngredienteViewModel : ViewModel() {
    private val repository = InventarioRepository()
    private val openFoodFactsRepository = OpenFoodFactsRepository()

    var uiState by mutableStateOf<IngredienteUiState>(IngredienteUiState.Idle)
        private set

    // Estado para el producto detectado por código de barras
    var productoEscaneado by mutableStateOf<ProductDetails?>(null)
        private set

    fun buscarProductoPorBarcode(barcode: String) {
        uiState = IngredienteUiState.Loading
        viewModelScope.launch {
            openFoodFactsRepository.buscarProducto(barcode).onSuccess { producto ->
                if (producto != null) {
                    productoEscaneado = producto
                    uiState = IngredienteUiState.Idle // O podrías crear un estado Success específico
                } else {
                    uiState = IngredienteUiState.Error("Producto no encontrado en Open Food Facts")
                }
            }.onFailure {
                uiState = IngredienteUiState.Error("Error al consultar el producto", it)
            }
        }
    }

    fun clearScannedProduct() {
        productoEscaneado = null
    }

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
        imagenBytes: ByteArray? = null,
        imageUrlFromApi: String? = null // --- NUEVO PARÁMETRO ---
    ) {
        val nombreTrimmed = nombre.trim()

        // --- FA-01: 'Campos vacíos' ---
        if (nombreTrimmed.isBlank() || cantidadStr.isBlank() || unidad.isBlank()) {
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
            val yaExiste = repository.existeIngrediente(nombreTrimmed)
            if (yaExiste) {
                uiState = IngredienteUiState.Error("El ingrediente '$nombreTrimmed' ya está en tu despensa.")
                return@launch
            }

            // --- NUEVA LÓGICA: SUBIR IMAGEN PRIMERO ---
            var urlImagenFinal: String? = imageUrlFromApi // Por defecto usamos la de la API si existe

            if (imagenBytes != null) {
                // Si el usuario tomó una foto manual, esta tiene prioridad
                val nombreArchivo = "foto_${System.currentTimeMillis()}"
                val resultadoImagen = repository.subirImagen(imagenBytes, nombreArchivo)

                resultadoImagen.onSuccess { url ->
                    urlImagenFinal = url 
                }.onFailure {
                    uiState = IngredienteUiState.Error("No se pudo subir la imagen del ingrediente", it)
                    return@launch
                }
            }
            // ------------------------------------------

            // Guardamos en Supabase incluyendo la URL de la imagen
            val resultado = repository.agregarIngrediente(
                nombre = nombreTrimmed,
                cantidad = cantidad,
                unidad = unidad,
                fechaCaducidad = fechaCaducidad.ifBlank { null },
                categoriaId = categoriaId,
                imagenUrl = urlImagenFinal
            )

            resultado.onSuccess {
                uiState = IngredienteUiState.Success
            }.onFailure {
                uiState = IngredienteUiState.Error("No se pudo guardar el ingrediente en la despensa", it)
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
    data class Error(val message: String, val throwable: Throwable? = null) : IngredienteUiState()
}