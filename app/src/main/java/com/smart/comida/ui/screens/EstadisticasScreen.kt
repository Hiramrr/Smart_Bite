package com.smart.comida.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.comida.ui.components.ErrorState
import com.smart.comida.ui.viewmodel.EstadisticasUiState
import com.smart.comida.ui.viewmodel.EstadisticasViewModel
import com.smart.comida.util.ErrorUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel,
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de Desperdicio", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is EstadisticasUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EstadisticasUiState.Error -> {
                val errorDetails = ErrorUtils.getErrorDetails(context, null)
                ErrorState(
                    title = errorDetails.title,
                    message = uiState.message,
                    onRetry = { viewModel.cargarEstadisticas() },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is EstadisticasUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Sin datos suficientes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No hay registros de consumo o desperdicio para el periodo actual.",
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    MonthSelector(viewModel, colorScheme)
                }
            }

            is EstadisticasUiState.Success -> {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    MonthSelector(viewModel, colorScheme)

                    SummaryCards(
                        totalConsumo = uiState.totalConsumo,
                        totalDesperdicio = uiState.totalDesperdicio,
                        cantidadConsumo = uiState.cantidadConsumo,
                        cantidadDesperdicio = uiState.cantidadDesperdicio,
                        colorScheme = colorScheme
                    )

                    ComparisonChart(
                        totalConsumo = uiState.totalConsumo,
                        totalDesperdicio = uiState.totalDesperdicio,
                        cantidadConsumo = uiState.cantidadConsumo,
                        cantidadDesperdicio = uiState.cantidadDesperdicio,
                        colorScheme = colorScheme
                    )

                    if (uiState.desperdicios.isNotEmpty()) {
                        WasteSummary(
                            desperdicios = uiState.desperdicios,
                            colorScheme = colorScheme
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    viewModel: EstadisticasViewModel,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { viewModel.mesAnterior() }) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Mes anterior",
                tint = colorScheme.primary
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${viewModel.nombreMes} ${viewModel.anio}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface
            )
            Text(
                "Periodo seleccionado",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = { viewModel.mesSiguiente() },
            enabled = viewModel.puedeAvanzar
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Mes siguiente",
                tint = if (viewModel.puedeAvanzar) colorScheme.primary else colorScheme.outline
            )
        }
    }
}

@Composable
private fun SummaryCards(
    totalConsumo: Int,
    totalDesperdicio: Int,
    cantidadConsumo: Float,
    cantidadDesperdicio: Float,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ShoppingCart,
            title = "Consumidos",
            count = totalConsumo,
            quantity = cantidadConsumo,
            color = Color(0xFF4CAF50),
            colorScheme = colorScheme
        )
        StatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Delete,
            title = "Desperdiciados",
            count = totalDesperdicio,
            quantity = cantidadDesperdicio,
            color = Color(0xFFE53935),
            colorScheme = colorScheme
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    count: Int,
    quantity: Float,
    color: Color,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "$count",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Text(
                "${formatQuantity(quantity)} uds.",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComparisonChart(
    totalConsumo: Int,
    totalDesperdicio: Int,
    cantidadConsumo: Float,
    cantidadDesperdicio: Float,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Comparativo mensual",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Consumo exitoso vs. desperdicio",
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            val maxValue = maxOf(totalConsumo, totalDesperdicio, 1)

            ChartBar(
                label = "Consumidos",
                value = totalConsumo,
                maxValue = maxValue,
                color = Color(0xFF4CAF50),
                colorScheme = colorScheme
            )
            Spacer(modifier = Modifier.height(16.dp))
            ChartBar(
                label = "Desperdiciados",
                value = totalDesperdicio,
                maxValue = maxValue,
                color = Color(0xFFE53935),
                colorScheme = colorScheme
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = Color(0xFF4CAF50), text = "Consumidos")
                LegendItem(color = Color(0xFFE53935), text = "Desperdiciados")
            }

            val totalSum = cantidadConsumo + cantidadDesperdicio
            if (totalSum > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Porcentaje de desperdicio: %.1f%%".format(
                        Locale.ROOT,
                        (cantidadDesperdicio / totalSum) * 100
                    ),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChartBar(
    label: String,
    value: Int,
    maxValue: Int,
    color: Color,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colorScheme.surfaceVariant)
        ) {
            val fraction = if (maxValue > 0) value.toFloat() / maxValue else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxSize()
                    .animateContentSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "$value",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WasteSummary(
    desperdicios: List<com.smart.comida.data.model.Desperdicio>,
    colorScheme: androidx.compose.material3.ColorScheme
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Ingredientes desperdiciados este mes",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            desperdicios.forEach { desperdicio ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            desperdicio.nombre,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onSurface
                        )
                        Text(
                            "Desechado: ${desperdicio.fechaDesecho.take(10)}",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${formatQuantity(desperdicio.cantidad)} ${desperdicio.unidad ?: ""}".trim(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE53935)
                    )
                }
            }
        }
    }
}

private fun formatQuantity(quantity: Float): String {
    return if (quantity == quantity.toLong().toFloat()) {
        quantity.toLong().toString()
    } else {
        String.format(Locale.ROOT, "%.1f", quantity)
    }
}
