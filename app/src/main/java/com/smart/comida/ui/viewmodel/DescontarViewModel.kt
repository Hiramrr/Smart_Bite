package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch

class DescontarViewModel : ViewModel() {
    private val repository = InventarioRepository()

    var ingrediente by mutableStateOf<Ingrediente?>(null)
        private set

    var uiState by mutableStateOf<DescontarUiState>(DescontarUiState.Idle)
        private set

    private var cantidadDisponiblePendiente: Float? = null

    fun cargarIngrediente(id: Int) {
        uiState = DescontarUiState.Loading
        viewModelScope.launch {
            repository.obtenerIngredientePorId(id).onSuccess {
                ingrediente = it
                uiState = DescontarUiState.Idle
            }.onFailure {
                uiState = DescontarUiState.Error("No se pudo cargar el ingrediente", it)
            }
        }
    }

    fun descontarCantidad(cantidadADescontarStr: String, unidadSeleccionada: String) {
        val ing = ingrediente ?: return
        
        // FA-01: Validar cantidad numérica
        val cantidadADescontar = cantidadADescontarStr.toFloatOrNull()
        if (cantidadADescontar == null || cantidadADescontar <= 0) {
            uiState = DescontarUiState.Error("Ingresa una cantidad válida mayor a cero.")
            return
        }

        // Lógica de conversión de unidades
        var cantidadProcesada = cantidadADescontar
        
        // Si el ingrediente está en Kg y descontamos en Gramos
        if (ing.unidad == "Kg" && unidadSeleccionada == "Gramos") {
            cantidadProcesada = cantidadADescontar / 1000f
        }
        // Si el ingrediente está en Litros y descontamos en ml
        else if (ing.unidad == "Litros" && unidadSeleccionada == "ml") {
            cantidadProcesada = cantidadADescontar / 1000f
        }
        // Nota: Si son iguales o piezas, la resta es directa.

        // FA-02: Validar stock disponible
        if (cantidadProcesada > ing.cantidad) {
            cantidadDisponiblePendiente = ing.cantidad
            uiState = DescontarUiState.StockExceeded(
                cantidadSolicitada = cantidadProcesada,
                cantidadDisponible = ing.cantidad,
                unidad = ing.unidad
            )
            return
        }

        ejecutarDescuento(ing, cantidadProcesada)
    }

    fun confirmarDescuentoDisponible() {
        val ing = ingrediente ?: return
        val cantidadDisponible = cantidadDisponiblePendiente ?: return
        cantidadDisponiblePendiente = null
        ejecutarDescuento(ing, cantidadDisponible)
    }

    fun corregirCantidad() {
        cantidadDisponiblePendiente = null
        uiState = DescontarUiState.Idle
    }

    private fun ejecutarDescuento(ing: Ingrediente, cantidad: Float) {
        uiState = DescontarUiState.Loading
        viewModelScope.launch {
            repository.descontarIngrediente(ing, cantidad).onSuccess {
                uiState = DescontarUiState.Success
            }.onFailure {
                uiState = DescontarUiState.Error("No se pudo actualizar el inventario", it)
            }
        }
    }

    fun resetState() {
        uiState = DescontarUiState.Idle
    }
}

sealed class DescontarUiState {
    object Idle : DescontarUiState()
    object Loading : DescontarUiState()
    object Success : DescontarUiState()
    data class StockExceeded(
        val cantidadSolicitada: Float,
        val cantidadDisponible: Float,
        val unidad: String?
    ) : DescontarUiState()
    data class Error(val message: String, val throwable: Throwable? = null) : DescontarUiState()
}
