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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.ui.viewmodel.DespensaUiState
import com.smart.comida.ui.viewmodel.DespensaViewModel

// Colores semánticos locales eliminados; usar MaterialTheme.colorScheme para soporte claro/oscuro

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DespensaScreen(
    viewModel: DespensaViewModel = viewModel(),
    onAgregarClick: () -> Unit,
    onHistorialDesperdicioClick: () -> Unit,
    onEditarClick: (Int) -> Unit,
    onVerDetalleClick: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val categorias = viewModel.categorias
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para el diálogo de confirmación
    var ingredienteADesperdicio by remember { mutableStateOf<Ingrediente?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarIngredientes()
    }

    LaunchedEffect(viewModel.mensajeOperacion) {
        val mensaje = viewModel.mensajeOperacion ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(mensaje)
        viewModel.limpiarMensajeOperacion()
    }

    if (ingredienteADesperdicio != null) {
        AlertDialog(
            onDismissRequest = { ingredienteADesperdicio = null },
            title = { Text("Registrar desperdicio") },
            text = { Text("¿Deseas registrar ${ingredienteADesperdicio?.nombre} como desperdicio? Se quitará del inventario activo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        ingredienteADesperdicio?.let { ing ->
                            viewModel.registrarComoDesperdicio(ing)
                        }
                        ingredienteADesperdicio = null
                    }
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredienteADesperdicio = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarClick,
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mi Despensa",
                        color = colorScheme.onBackground,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onHistorialDesperdicioClick) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Ver historial de desperdicio",
                                tint = colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.actualizarBusqueda(it) },
                    placeholder = { Text("Buscar ingredientes", color = colorScheme.onSurfaceVariant, fontSize = 16.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = colorScheme.surfaceVariant,
                        unfocusedContainerColor = colorScheme.surfaceVariant,
                        cursorColor = colorScheme.onBackground,
                        focusedTextColor = colorScheme.onBackground,
                        unfocusedTextColor = colorScheme.onBackground
                    ),
                    singleLine = true
                )
            }

            item {
                var expandirCaducidad by remember { mutableStateOf(false) }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = viewModel.filtroSeleccionado == null && viewModel.diasFiltroCaducidad == null,
                            onClick = {
                                viewModel.seleccionarFiltroCategoria(null)
                                viewModel.seleccionarFiltroCaducidad(null)
                            },
                            label = { Text("Todos", color = colorScheme.onSurface) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary,
                                selectedLabelColor = colorScheme.onPrimary,
                                containerColor = colorScheme.surfaceVariant
                            ),
                            shape = CircleShape,
                            border = null
                        )
                    }
                    item {
                        Box {
                            FilterChip(
                                selected = viewModel.diasFiltroCaducidad != null,
                                onClick = { expandirCaducidad = true },
                                label = {
                                    Text(
                                        text = if (viewModel.diasFiltroCaducidad != null) "Caducan: ${viewModel.diasFiltroCaducidad} días" else "Caducidad",
                                        color = colorScheme.onSurface
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = colorScheme.error,
                                    selectedLabelColor = colorScheme.onError,
                                    containerColor = colorScheme.surfaceVariant
                                ),
                                shape = CircleShape,
                                border = null
                            )

                            DropdownMenu(
                                expanded = expandirCaducidad,
                                onDismissRequest = { expandirCaducidad = false },
                                modifier = Modifier.background(colorScheme.surfaceVariant)
                            ) {
                                listOf(3, 5, 7).forEach { dias ->
                                    DropdownMenuItem(
                                        text = { Text("Próximos $dias días", color = colorScheme.onSurface) },
                                        onClick = {
                                            viewModel.seleccionarFiltroCaducidad(dias)
                                            expandirCaducidad = false
                                        }
                                    )
                                }
                                if (viewModel.diasFiltroCaducidad != null) {
                                    Divider(color = colorScheme.outline.copy(alpha = 0.5f))
                                    DropdownMenuItem(
                                        text = { Text("Quitar filtro", color = colorScheme.onSurface) },
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
                        FilterChip(
                            selected = viewModel.filtroSeleccionado?.id == categoria.id,
                            onClick = { viewModel.seleccionarFiltroCategoria(categoria) },
                            label = { Text(categoria.nombre, color = colorScheme.onSurface) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary,
                                selectedLabelColor = colorScheme.onPrimary,
                                containerColor = colorScheme.surfaceVariant
                            ),
                            shape = CircleShape,
                            border = null
                        )
                    }
                }
            }

            when (uiState) {
                is DespensaUiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colorScheme.primary)
                        }
                    }
                }
                is DespensaUiState.Error -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = uiState.message, color = colorScheme.error, fontSize = 16.sp)
                            Button(
                                onClick = { viewModel.cargarIngredientes() },
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                is DespensaUiState.Success -> {
                    val ingredientes = uiState.ingredientes
                    val categoriasPorId = categorias.associateBy { it.id }

                    if (ingredientes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No se encontraron ingredientes para este filtro.",
                                    color = colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        val lowStock = ingredientes.filter { it.cantidad <= 5 }
                        val recentlyAdded = ingredientes

                        if (lowStock.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Poco Inventario", color = colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ver Todo", tint = colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp)
                                ) {
                                    items(lowStock) { ingrediente ->
                                        LowStockCard(
                                            ingrediente = ingrediente,
                                            categoriaNombre = categoriasPorId[ingrediente.categoriaId]?.nombre ?: "Sin categoría",
                                            onClick = onVerDetalleClick,
                                            onEditarClick = { ingrediente.id?.let(onEditarClick) },
                                            onEliminarClick = { ingredienteADesperdicio = ingrediente }
                                        )
                                    }
                                }
                            }
                        }

                        if (recentlyAdded.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Inventario Activo", color = colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ver Todo", tint = colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            items(recentlyAdded) { ingrediente ->
                                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                    RecentlyAddedCard(
                                        ingrediente = ingrediente,
                                        categoriaNombre = categoriasPorId[ingrediente.categoriaId]?.nombre ?: "Sin categoría",
                                        onClick = onVerDetalleClick,
                                        onEditarClick = { ingrediente.id?.let(onEditarClick) },
                                        onEliminarClick = { ingredienteADesperdicio = ingrediente }
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockCard(
    ingrediente: Ingrediente,
    categoriaNombre: String,
    onClick: (Int) -> Unit,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(240.dp)
            .clickable { ingrediente.id?.let { onClick(it) } },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(modifier = Modifier.weight(1f).padding(16.dp)) {
                if (!ingrediente.imagenUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ingrediente.imagenUrl,
                        contentDescription = ingrediente.nombre,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(text = ingrediente.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = categoriaNombre,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    Text(text = "${ingrediente.cantidad} ${ingrediente.unidad ?: "restantes"}", fontSize = 16.sp, color = colorScheme.onSurface)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$categoriaNombre • ${ingrediente.cantidad}",
                            color = colorScheme.onPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onEditarClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = onEliminarClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Registrar desperdicio",
                            tint = colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = colorScheme.onPrimary, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecentlyAddedCard(
    ingrediente: Ingrediente,
    categoriaNombre: String,
    onClick: (Int) -> Unit,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { ingrediente.id?.let { onClick(it) } },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!ingrediente.imagenUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ingrediente.imagenUrl,
                    contentDescription = ingrediente.nombre,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = ingrediente.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Text(
                    text = "$categoriaNombre • ${ingrediente.cantidad} ${ingrediente.unidad ?: "restantes"}",
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onEditarClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEliminarClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Registrar desperdicio",
                    tint = colorScheme.onSurfaceVariant
                )
            }

            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ver", tint = colorScheme.onSurfaceVariant)
        }
    }
}
