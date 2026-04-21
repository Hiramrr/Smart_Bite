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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
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

val DarkBackground = Color(0xFF1C1C1E)
val CardBackground = Color(0xFFF3F4ED)
val DarkCardBackground = Color(0xFF2C2C2E)
val LightYellow = Color(0xFFEBEB9B)
val GreenAccent = Color(0xFF637C5B)

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
    var searchQuery by remember { mutableStateOf("") }
    
    // Estado para el diálogo de confirmación
    var ingredienteAEliminar by remember { mutableStateOf<Ingrediente?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarIngredientes()
    }

    if (ingredienteAEliminar != null) {
        AlertDialog(
            onDismissRequest = { ingredienteAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar ${ingredienteAEliminar?.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        ingredienteAEliminar?.let { ing ->
                            if (ing.id != null) {
                                viewModel.eliminarIngrediente(ing.id, ing.imagenUrl)
                            }
                        }
                        ingredienteAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredienteAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarClick,
                containerColor = LightYellow,
                contentColor = Color.Black,
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
                Text(
                    text = "Mi Despensa",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar ingredientes", color = Color.Gray, fontSize = 16.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = DarkCardBackground,
                        unfocusedContainerColor = DarkCardBackground,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = viewModel.filtroSeleccionado == null && !viewModel.filtroPorCaducar,
                            onClick = { viewModel.seleccionarFiltroCategoria(null) },
                            label = { Text("Todos", color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenAccent,
                                containerColor = DarkCardBackground
                            ),
                            shape = CircleShape,
                            border = null
                        )
                    }
                    item {
                        FilterChip(
                            selected = viewModel.filtroPorCaducar,
                            onClick = { viewModel.toggleFiltroPorCaducar() },
                            label = { Text("Próximos a caducar", color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFD47979), // Un color rojizo para caducidad
                                containerColor = DarkCardBackground
                            ),
                            shape = CircleShape,
                            border = null
                        )
                    }
                    items(categorias) { categoria ->
                        FilterChip(
                            selected = viewModel.filtroSeleccionado?.id == categoria.id,
                            onClick = { viewModel.seleccionarFiltroCategoria(categoria) },
                            label = { Text(categoria.nombre, color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenAccent,
                                containerColor = DarkCardBackground
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
                            CircularProgressIndicator(color = LightYellow)
                        }
                    }
                }
                is DespensaUiState.Error -> {
                    item {
                        Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is DespensaUiState.Success -> {
                    val ingredientes = uiState.ingredientes.filter {
                        it.nombre.contains(searchQuery, ignoreCase = true)
                    }
                    val categoriasPorId = categorias.associateBy { it.id }

                    if (ingredientes.isEmpty()) {
                        item {
                            Text("No se encontraron ingredientes.", color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp))
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
                                    Text("Poco Inventario", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ver Todo", tint = Color.Gray)
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
                                            onEliminarClick = { ingredienteAEliminar = ingrediente }
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
                                    Text("Agregados Recientemente", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ver Todo", tint = Color.Gray)
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
                                        onEliminarClick = { ingredienteAEliminar = ingrediente }
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
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(240.dp)
            .clickable { ingrediente.id?.let { onClick(it) } },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
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
                            .background(Color.LightGray, RoundedCornerShape(16.dp))
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(text = ingrediente.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFE2E2E2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = categoriaNombre,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.DarkGray
                        )
                    }
                    Text(text = "${ingrediente.cantidad} ${ingrediente.unidad ?: "restantes"}", fontSize = 16.sp, color = Color.Black)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(LightYellow)
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
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$categoriaNombre • ${ingrediente.cantidad}",
                            color = Color.Black,
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
                            tint = Color.Black.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(onClick = onEliminarClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Black.copy(alpha = 0.6f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFD4D479),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(4.dp))
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { ingrediente.id?.let { onClick(it) } },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
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
                        .background(Color.LightGray, RoundedCornerShape(16.dp))
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = ingrediente.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(
                    text = "$categoriaNombre • ${ingrediente.cantidad} ${ingrediente.unidad ?: "restantes"}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onEditarClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = Color.Gray
                )
            }

            IconButton(onClick = onEliminarClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color.Gray
                )
            }
            
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ver", tint = Color.Gray)
        }
    }
}
