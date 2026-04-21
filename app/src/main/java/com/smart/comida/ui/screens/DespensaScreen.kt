package com.smart.comida.ui.screens

import androidx.compose.foundation.clickable // <-- NUEVO: Importación para el clic
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.ui.viewmodel.DespensaUiState
import com.smart.comida.ui.viewmodel.DespensaViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DespensaScreen(
    viewModel: DespensaViewModel = viewModel(),
    onAgregarClick: () -> Unit,
    onEditarClick: (Int) -> Unit,
    onVerDetalleClick: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val categorias = viewModel.categorias

    LaunchedEffect(Unit) {
        viewModel.cargarIngredientes()
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Mi Despensa") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarClick,
                containerColor = MaterialTheme.colorScheme.primary, // Se pintará Turquesa
                contentColor = MaterialTheme.colorScheme.onPrimary, // El ícono de "+" será blanco
                shape = RoundedCornerShape(16.dp) // Un cuadrado con bordes suaves es más moderno que un círculo perfecto
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->
        // Usamos un Column para poner los filtros arriba y la cuadrícula abajo
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // --- SECCIÓN DE FILTROS (LazyRow) ---
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filtro "Todos"
                item {
                    FilterChip(
                        selected = viewModel.filtroSeleccionado == null && !viewModel.filtroPorCaducar,
                        onClick = { viewModel.seleccionarFiltroCategoria(null) },
                        label = { Text("Todos") }
                    )
                }
                // Filtro "Por Caducar"
                item {
                    FilterChip(
                        selected = viewModel.filtroPorCaducar,
                        onClick = { viewModel.toggleFiltroPorCaducar() },
                        label = { Text("Por Caducar") }
                    )
                }

                // Filtros dinámicos desde Supabase (Lácteos, Carnes, etc.)
                items(categorias) { categoria ->
                    FilterChip(
                        selected = viewModel.filtroSeleccionado?.id == categoria.id,
                        onClick = { viewModel.seleccionarFiltroCategoria(categoria) },
                        label = { Text(categoria.nombre) }
                    )
                }
            }

            // --- SECCIÓN DE LA CUADRÍCULA ---
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState) {
                    is DespensaUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is DespensaUiState.Error -> {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is DespensaUiState.Success -> {
                        if (uiState.ingredientes.isEmpty()) {
                            Text("No hay ingredientes en esta categoría.", modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(uiState.ingredientes) { ingrediente ->
                                    IngredienteCard(
                                        ingrediente = ingrediente,
                                        onEditarClick = {
                                            if (ingrediente.id != null) onEditarClick(ingrediente.id)
                                        },
                                        onEliminarClick = {
                                            if (ingrediente.id != null) {
                                                viewModel.eliminarIngrediente(
                                                    id = ingrediente.id,
                                                    imagenUrl = ingrediente.imagenUrl
                                                )
                                            }
                                        },
                                        // --- NUEVO: PASAMOS EL CLIC PARA NAVEGAR AL DETALLE ---
                                        onCardClick = {
                                            if (ingrediente.id != null) onVerDetalleClick(ingrediente.id)
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
}

// EL DISEÑO DE TU TARJETA (VERSIÓN MEJORADA)
@Composable
fun IngredienteCard(
    ingrediente: Ingrediente,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit,
    onCardClick: () -> Unit // --- NUEVO: Parámetro para manejar el clic en la tarjeta ---
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable { onCardClick() }, // --- NUEVO: Hace que toda la tarjeta reaccione al toque ---
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // --- 1. SECCIÓN SUPERIOR: Fila de Acciones ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = onEliminarClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = onEditarClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (!ingrediente.imagenUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ingrediente.imagenUrl,
                        contentDescription = "Foto de ${ingrediente.nombre}",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = ingrediente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${ingrediente.cantidad} ${ingrediente.unidad ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Caduca: ${ingrediente.fechaCaducidad ?: "S/F"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                    maxLines = 1
                )
            }
        }
    }
}