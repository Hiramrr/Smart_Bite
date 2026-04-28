package com.smart.comida.ui.screens

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
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
    onVerDetalleClick: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val categorias = viewModel.categorias
    var expandirCaducidad by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundWhite,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDark,
                    navigationIconContentColor = TextDark,
                    actionIconContentColor = TextDark
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
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White,
                            containerColor = BackgroundWhite,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = viewModel.filtroSeleccionado == null && viewModel.diasFiltroCaducidad == null, borderColor = GrayBorder),
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
                                selectedContainerColor = Color(0xFFD47979),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White,
                                selectedTrailingIconColor = Color.White,
                                containerColor = BackgroundWhite,
                                labelColor = TextGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = viewModel.diasFiltroCaducidad != null, borderColor = GrayBorder),
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
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White,
                            containerColor = BackgroundWhite,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = GrayBorder),
                        shape = CircleShape
                    )
                }
            }

            // List of Ingredients
            when (uiState) {
                is DespensaUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is DespensaUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.message, color = RedExpiring)
                    }
                }
                is DespensaUiState.Success -> {
                    if (uiState.ingredientes.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No se encontraron ingredientes.", color = TextGray)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.ingredientes) { ingrediente ->
                                IngredienteListItem(ingrediente = ingrediente, onClick = { ingrediente.id?.let(onVerDetalleClick) })
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
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
            Box(modifier = Modifier.size(48.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(ingrediente.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Text("${ingrediente.cantidad} ${ingrediente.unidad ?: ""}", fontSize = 14.sp, color = TextGray)
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
                "Caducado" -> Color.Red
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
    Divider(color = BackgroundWhite)
}
