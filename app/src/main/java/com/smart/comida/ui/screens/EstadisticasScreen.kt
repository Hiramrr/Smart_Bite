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
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    MonthSelector(viewModel, colorScheme)
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(40.dp))
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = colorScheme.outline.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Sin datos suficientes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No hay registros de consumo o desperdicio para el periodo de ${viewModel.nombreMes}.",
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }

                    ComparisonChart(
                        totalConsumo = 0,
                        totalDesperdicio = 0,
                        cantidadConsumo = 0f,
                        cantidadDesperdicio = 0f,
                        colorScheme = colorScheme
                    )
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
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.mesAnterior() },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surface)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Mes anterior",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${viewModel.nombreMes} ${viewModel.anio}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Text(
                    "Periodo seleccionado",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { viewModel.mesSiguiente() },
                enabled = viewModel.puedeAvanzar,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (viewModel.puedeAvanzar) colorScheme.surface else Color.Transparent)
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Mes siguiente",
                    tint = if (viewModel.puedeAvanzar) colorScheme.primary else colorScheme.outline.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "$count",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "items",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Text(
                "Total: ${formatQuantity(quantity)} uds.",
                fontSize = 12.sp,
                color = colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Comparativo mensual",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        "Gestión vs. desperdicio",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                
                val totalSum = cantidadConsumo + cantidadDesperdicio
                if (totalSum > 0) {
                    val percentage = (cantidadDesperdicio / totalSum) * 100
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (percentage > 20) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "%.1f%%".format(Locale.ROOT, percentage),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (percentage > 20) Color(0xFFD32F2F) else Color(0xFF388E3C)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LegendItem(color = Color(0xFF4CAF50), text = "Consumidos")
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem(color = Color(0xFFE53935), text = "Desperdiciados")
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                "$value",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
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
    Column {
        Text(
            "Detalle de Desperdicios",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                desperdicios.forEachIndexed { index, desperdicio ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFEBEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                desperdicio.nombre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )
                            Text(
                                "Desechado el ${desperdicio.fechaDesecho.take(10)}",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${formatQuantity(desperdicio.cantidad)} ${desperdicio.unidad ?: ""}".trim(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    }
                    
                    if (index < desperdicios.size - 1) {
                        Spacer(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )
                    }
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
