package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.smart.comida.ui.components.ShimmerResumenCards
import com.smart.comida.ui.components.ShimmerIngredientVencerCard
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.smart.comida.util.ErrorUtils
import coil.compose.AsyncImage
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.ui.components.EmptyState
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.DespensaUiState
import com.smart.comida.ui.viewmodel.DespensaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DespensaViewModel,
    userName: String = "Usuario",
    onVerTodosClick: () -> Unit,
    onVerDetalleClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.cargarIngredientes()
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mi Despensa", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { viewModel.cargarIngredientes() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refrescar"
                        )
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Greeting section
            Column {
                Text("¡Hola, $userName! 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                Text("¿Qué vamos a cocinar hoy?", fontSize = 16.sp, color = colorScheme.onSurfaceVariant)
            }

            // Search Bar
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                placeholder = { Text("Buscar ingrediente...", color = colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colorScheme.outline,
                    focusedBorderColor = colorScheme.primary,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedContainerColor = colorScheme.surface
                ),
                singleLine = true
            )

            // Category Chips (Horizontal Scroll)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.categorias) { categoria ->
                    val isSelected = viewModel.filtroSeleccionado?.id == categoria.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            if (isSelected) viewModel.seleccionarFiltroCategoria(null)
                            else viewModel.seleccionarFiltroCategoria(categoria)
                        },
                        label = { Text(categoria.nombre, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary,
                            containerColor = Color.Transparent,
                            labelColor = colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = colorScheme.outline
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Resumen de tu despensa
            var isResumenExpanded by remember { mutableStateOf(true) }
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isResumenExpanded = !isResumenExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Resumen de tu despensa", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                    Icon(
                        imageVector = if (isResumenExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir/Minimizar",
                        tint = colorScheme.onBackground
                    )
                }
                
                AnimatedVisibility(visible = isResumenExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResumenItem(
                            viewModel.resumen.total.toString(),
                            "Ingredientes",
                            colorScheme.primaryContainer,
                            colorScheme.primary,
                            Icons.Default.Kitchen
                        )
                        ResumenItem(
                            viewModel.resumen.porVencer.toString(),
                            "Por vencer",
                            colorScheme.tertiaryContainer,
                            colorScheme.tertiary,
                            Icons.Default.Warning
                        )
                        ResumenItem(
                            viewModel.resumen.bajosStock.toString(),
                            "Bajos en stock",
                            colorScheme.secondaryContainer,
                            colorScheme.secondary,
                            Icons.Default.TrendingDown
                        )
                    }
                }
            }

            // Buen momento para comprar
            val hayPorVencer = viewModel.resumen.porVencer > 0
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (hayPorVencer) colorScheme.primaryContainer else colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = if (hayPorVencer) null else androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (hayPorVencer) "Buen momento para comprar 🛒" else "Tu despensa está tranquila",
                            fontWeight = FontWeight.Bold,
                            color = if (hayPorVencer) colorScheme.primary else colorScheme.onSurface
                        )
                        Text(
                            if (hayPorVencer) {
                                "Tienes ${viewModel.resumen.porVencer} ingredientes por vencer en los próximos 7 días."
                            } else {
                                "No tienes ingredientes por vencer esta semana."
                            },
                            fontSize = 12.sp,
                            color = if (hayPorVencer) colorScheme.primary else colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onVerTodosClick,
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surface, contentColor = colorScheme.primary),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Ver lista", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Image placeholder
                    Box(modifier = Modifier.size(80.dp).background(Color.Transparent)) {
                        // In reality an image goes here
                    }
                }
            }

            // Por vencer pronto
            when (uiState) {
                is DespensaUiState.Loading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Por vencer pronto", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(4) {
                                ShimmerIngredientVencerCard()
                            }
                        }
                    }
                }
                is DespensaUiState.Error -> {
                    val errorDetails = ErrorUtils.getErrorDetails(context, uiState.throwable)
                    com.smart.comida.ui.components.ErrorState(
                        title = errorDetails.title,
                        message = uiState.message.ifBlank { errorDetails.message },
                        onRetry = { viewModel.cargarIngredientes() },
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    )
                }
                is DespensaUiState.Success -> {
                    val ingredientes = uiState.ingredientes
                        .filter { diasHastaCaducidad(it) != null }
                        .sortedBy { diasHastaCaducidad(it) }
                        .take(4)
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Por vencer pronto", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)
                        Text(
                            "Ver todos",
                            fontSize = 14.sp,
                            color = colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onVerTodosClick() }
                        )
                    }
                    
                    if (ingredientes.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Kitchen,
                            title = "Nada por vencer pronto",
                            description = "Cuando un ingrediente esté cerca de caducar, aparecerá aquí.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(ingredientes) { ingrediente ->
                                IngredienteVencerCard(ingrediente) { ingrediente.id?.let(onVerDetalleClick) }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
fun ResumenItem(value: String, label: String, bgColor: Color, iconColor: Color, icon: ImageVector) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onBackground)
        Text(label, fontSize = 10.sp, color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun IngredienteVencerCard(ingrediente: Ingrediente, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val diasRestantes = diasHastaCaducidad(ingrediente)
    val caducidadText = when (diasRestantes) {
        null -> ""
        0L -> "Hoy"
        1L -> "1 día"
        else -> "$diasRestantes días"
    }
    Card(
        modifier = Modifier.width(100.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (caducidadText.isNotEmpty()) {
                Surface(
                    color = colorScheme.errorContainer,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        caducidadText,
                        color = colorScheme.error,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            if (!ingrediente.imagenUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ingrediente.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(48.dp).background(colorScheme.surfaceVariant, CircleShape))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(ingrediente.nombre, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground, maxLines = 1, textAlign = TextAlign.Center)
            Text("${ingrediente.cantidad} ${ingrediente.unidad ?: ""}", fontSize = 10.sp, color = colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

private fun diasHastaCaducidad(ingrediente: Ingrediente): Long? {
    val fechaCaducidad = ingrediente.fechaCaducidad ?: return null
    return runCatching {
        val hoy = java.time.LocalDate.now()
        val fecha = java.time.LocalDate.parse(fechaCaducidad)
        java.time.temporal.ChronoUnit.DAYS.between(hoy, fecha)
    }.getOrNull()?.takeIf { it in 0..7 }
}
