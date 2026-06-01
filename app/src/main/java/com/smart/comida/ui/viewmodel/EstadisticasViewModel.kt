package com.smart.comida.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smart.comida.data.model.Consumo
import com.smart.comida.data.model.Desperdicio
import com.smart.comida.data.repository.InventarioRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class EstadisticasViewModel : ViewModel() {
    private val repository = InventarioRepository()

    var uiState by mutableStateOf<EstadisticasUiState>(EstadisticasUiState.Loading)
        private set

    var mes by mutableStateOf(LocalDate.now().monthValue)
        private set

    var anio by mutableStateOf(LocalDate.now().year)
        private set

    val nombreMes: String
        get() {
            val month = java.time.Month.of(mes)
            return month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { it.uppercase() }
        }

    val puedeAvanzar: Boolean
        get() {
            val ahora = LocalDate.now()
            return anio < ahora.year || (anio == ahora.year && mes < ahora.monthValue)
        }

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        uiState = EstadisticasUiState.Loading
        viewModelScope.launch {
            val consumosResult = repository.obtenerConsumosPorMes(mes, anio)
            val desperdiciosResult = repository.obtenerDesperdiciosPorMes(mes, anio)

            if (consumosResult.isFailure || desperdiciosResult.isFailure) {
                uiState = EstadisticasUiState.Error("Error al cargar las estadísticas")
                return@launch
            }

            val consumos = consumosResult.getOrDefault(emptyList())
            val desperdicios = desperdiciosResult.getOrDefault(emptyList())

            val totalConsumo = consumos.size
            val cantidadConsumo = consumos.sumOf { it.cantidad.toDouble() }.toFloat()

            val totalDesperdicio = desperdicios.size
            val cantidadDesperdicio = desperdicios.sumOf { it.cantidad.toDouble() }.toFloat()

            if (totalConsumo == 0 && totalDesperdicio == 0) {
                uiState = EstadisticasUiState.Empty
            } else {
                uiState = EstadisticasUiState.Success(
                    totalConsumo = totalConsumo,
                    totalDesperdicio = totalDesperdicio,
                    cantidadConsumo = cantidadConsumo,
                    cantidadDesperdicio = cantidadDesperdicio,
                    consumos = consumos,
                    desperdicios = desperdicios
                )
            }
        }
    }

    fun mesAnterior() {
        if (mes == 1) {
            mes = 12
            anio -= 1
        } else {
            mes -= 1
        }
        cargarEstadisticas()
    }

    fun mesSiguiente() {
        if (!puedeAvanzar) return
        if (mes == 12) {
            mes = 1
            anio += 1
        } else {
            mes += 1
        }
        cargarEstadisticas()
    }
}

sealed class EstadisticasUiState {
    object Loading : EstadisticasUiState()
    data class Success(
        val totalConsumo: Int,
        val totalDesperdicio: Int,
        val cantidadConsumo: Float,
        val cantidadDesperdicio: Float,
        val consumos: List<Consumo>,
        val desperdicios: List<Desperdicio>
    ) : EstadisticasUiState()
    object Empty : EstadisticasUiState()
    data class Error(val message: String) : EstadisticasUiState()
}
