package com.smart.comida.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.DespensaUiState
import com.smart.comida.ui.viewmodel.DespensaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DespensaListScreen(
    viewModel: DespensaViewModel,
    onBackClick: () -> Unit,
    onVerDetalleClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState = viewModel.uiState
    val categorias = viewModel.categorias
    var expandirCaducidad by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Mi Despensa", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Ordenar")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = viewModel.filtroSeleccionado == null && viewModel.diasFiltroCaducidad == null,
                        onClick = { 
                            viewModel.seleccionarFiltroCategoria(null) 
                            viewModel.seleccionarFiltroCaducidad(null)
                        },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary,
                            containerColor = colorScheme.background,
                            labelColor = colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = viewModel.filtroSeleccionado == null && viewModel.diasFiltroCaducidad == null, borderColor = colorScheme.outline),
                        shape = CircleShape
                    )
                }
                
                // Filtro de Caducidad
                item {
                    Box {
                        FilterChip(
                            selected = viewModel.diasFiltroCaducidad != null,
                            onClick = { expandirCaducidad = true },
                            label = { 
                                Text(if (viewModel.diasFiltroCaducidad != null) "Caducan: ${viewModel.diasFiltroCaducidad}d" else "Caducidad") 
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.error,
                                selectedLabelColor = colorScheme.onError,
                                selectedLeadingIconColor = colorScheme.onError,
                                selectedTrailingIconColor = colorScheme.onError,
                                containerColor = colorScheme.background,
                                labelColor = colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = viewModel.diasFiltroCaducidad != null, borderColor = colorScheme.outline),
                            shape = CircleShape
                        )
                        
                        DropdownMenu(
                            expanded = expandirCaducidad,
                            onDismissRequest = { expandirCaducidad = false }
                        ) {
                            listOf(3, 5, 7).forEach { dias ->
                                DropdownMenuItem(
                                    text = { Text("Próximos $dias días") },
                                    onClick = {
                                        viewModel.seleccionarFiltroCaducidad(dias)
                                        expandirCaducidad = false
                                    }
                                )
                            }
                            if (viewModel.diasFiltroCaducidad != null) {
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Quitar filtro") },
                                    onClick = {
                                        viewModel.seleccionarFiltroCaducidad(null)
                                        expandirCaducidad = false
                                    }
                                )
                            }
                        }
                    }
                }

                    items(categorias) { categoria ->
                    val isSelected = viewModel.filtroSeleccionado?.id == categoria.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.seleccionarFiltroCategoria(categoria) },
                        label = { Text(categoria.nombre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primary,
                            selectedLabelColor = colorScheme.onPrimary,
                            containerColor = colorScheme.background,
                            labelColor = colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = colorScheme.outline),
                        shape = CircleShape
                    )
                }
            }

            // List of Ingredients
            when (uiState) {
                is DespensaUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                }
                is DespensaUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.message, color = colorScheme.error)
                    }
                }
                is DespensaUiState.Success -> {
                    if (uiState.ingredientes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron ingredientes.", color = colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.ingredientes, key = { it.id ?: it.hashCode() }) { ingrediente ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        when (value) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                viewModel.usarIngrediente(ingrediente)
                                                false
                                            }
                                            SwipeToDismissBoxValue.Settled -> false
                                            else -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = true,
                                    enableDismissFromEndToStart = false,
                                    backgroundContent = {
                                        val color by animateColorAsState(
                                            when (dismissState.targetValue) {
                                                SwipeToDismissBoxValue.StartToEnd -> colorScheme.primary.copy(alpha = 0.8f)
                                                SwipeToDismissBoxValue.Settled -> Color.Transparent
                                                else -> Color.Transparent
                                            },
                                            label = "swipeBg"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Usar", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    },
                                    content = {
                                        IngredienteListItem(ingrediente = ingrediente, onClick = { ingrediente.id?.let(onVerDetalleClick) })
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IngredienteListItem(ingrediente: Ingrediente, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!ingrediente.imagenUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ingrediente.imagenUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.size(48.dp).background(colorScheme.surfaceVariant, RoundedCornerShape(8.dp)))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(ingrediente.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colorScheme.onSurface)
            Text("${ingrediente.cantidad} ${ingrediente.unidad ?: ""}", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
        }

        // Cálculo de días reales
        val diasRestantesText = if (!ingrediente.fechaCaducidad.isNullOrEmpty()) {
            try {
                val hoy = java.time.LocalDate.now()
                val fechaCad = java.time.LocalDate.parse(ingrediente.fechaCaducidad)
                val diff = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaCad)

                when {
                    diff < 0 -> "Caducado"
                    diff == 0L -> "Caduca hoy"
                    diff == 1L -> "1 día"
                    else -> "$diff días"
                }
            } catch (e: Exception) {
                ""
            }
        } else {
            ""
        }

        if (diasRestantesText.isNotEmpty()) {
            val textColor = when (diasRestantesText) {
                "Caducado" -> MaterialTheme.colorScheme.error
                "Caduca hoy" -> RedExpiring
                else -> OrangeExpiring
            }
            Text(
                text = diasRestantesText,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
