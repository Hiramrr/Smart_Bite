package com.smart.comida.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.data.model.ArticuloCompra
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.ListaComprasUiState
import com.smart.comida.ui.viewmodel.ListaComprasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaComprasScreen(
    viewModel: ListaComprasViewModel = viewModel(),
    onSettingsClick: () -> Unit = {}
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTab by remember { mutableStateOf(0) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarArticulos()
    }

    LaunchedEffect(viewModel.mensajeOperacion) {
        viewModel.mensajeOperacion?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeOperacion()
        }
    }

    if (mostrarDialogo) {
        var nombre by remember { mutableStateOf("") }
        var cantidad by remember { mutableStateOf("") }
        var errorNombre by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Agregar a la lista", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            if (it.isNotBlank()) errorNombre = false
                        },
                        label = { Text("Nombre del producto") },
                        isError = errorNombre,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    if (errorNombre) {
                        Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad esperada (Ej: 2 kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombre.isBlank()) {
                            errorNombre = true
                        } else {
                            viewModel.agregarArticulo(nombre, cantidad)
                            mostrarDialogo = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Agregar", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Lista de compras", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
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
        },
        bottomBar = {
            PaddingValues(16.dp).let { padding ->
                Button(
                    onClick = { mostrarDialogo = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar producto", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                TabButton(text = "Actual", isSelected = selectedTab == 0, modifier = Modifier.weight(1f)) { selectedTab = 0 }
                TabButton(text = "Historial", isSelected = selectedTab == 1, modifier = Modifier.weight(1f)) { selectedTab = 1 }
            }

            when (uiState) {
                is ListaComprasUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                }
                is ListaComprasUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiState.message, color = colorScheme.error)
                    }
                }
                is ListaComprasUiState.Success -> {
                    val articulos = uiState.articulos

                    if (articulos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(bottom = 56.dp), contentAlignment = Alignment.Center) {
                            Text("Tu lista de compras está vacía.", color = colorScheme.onSurfaceVariant)
                        }
                    } else {
                        // Progress
                        val comprados = articulos.count { it.estado == "Comprado" }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Progreso", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                            Text("$comprados de ${articulos.size}", fontSize = 14.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                        LinearProgressIndicator(
                            progress = { if (articulos.isEmpty()) 0f else comprados.toFloat() / articulos.size.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = colorScheme.primary,
                            trackColor = colorScheme.primaryContainer
                        )

                        // List
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(articulos, key = { it.id ?: it.hashCode() }) { articulo ->
                                val isChecked = articulo.estado == "Comprado"
                                val textDecoration = if (isChecked) TextDecoration.LineThrough else null
                                val textColor = if (isChecked) colorScheme.onSurfaceVariant else colorScheme.onSurface

                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        when (value) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                articulo.id?.let { viewModel.eliminarArticulo(it) }
                                                false
                                            }
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                articulo.id?.let { viewModel.marcarComoComprado(it, articulo.estado) }
                                                false
                                            }
                                            SwipeToDismissBoxValue.Settled -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = true,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {
                                        val direction = dismissState.dismissDirection

                                        val color by animateColorAsState(
                                            when (dismissState.targetValue) {
                                                SwipeToDismissBoxValue.EndToStart -> colorScheme.error.copy(alpha = 0.8f)
                                                SwipeToDismissBoxValue.StartToEnd -> PrimaryGreen.copy(alpha = 0.8f)
                                                SwipeToDismissBoxValue.Settled -> Color.Transparent
                                            },
                                            label = "bgColor"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(color)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd)
                                                Alignment.CenterStart else Alignment.CenterEnd
                                        ) {
                                            when (direction) {
                                                SwipeToDismissBoxValue.EndToStart -> {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                                    }
                                                }
                                                SwipeToDismissBoxValue.StartToEnd -> {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            if (isChecked) "Pendiente" else "Comprado",
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                else -> {}
                                            }
                                        }
                                    },
                                    content = {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(colorScheme.surface, RoundedCornerShape(16.dp))
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(articulo.nombre, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Medium, textDecoration = textDecoration)
                                                articulo.cantidadEsperada?.let {
                                                    val displayCantidad = if (articulo.unidad != null) "$it ${articulo.unidad}" else it.toString()
                                                    Text(displayCantidad, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, textDecoration = textDecoration)
                                                }
                                            }
                                        }
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
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val bgColor = if (isSelected) colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant

    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
