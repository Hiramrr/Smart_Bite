package com.smart.comida.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.smart.comida.util.ErrorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.data.model.ArticuloCompra
import com.smart.comida.ui.components.EmptyState
import com.smart.comida.ui.components.ShimmerComprasList
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.ListaComprasUiState
import com.smart.comida.ui.viewmodel.ListaComprasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaComprasScreen(
    viewModel: ListaComprasViewModel = viewModel(),
    onSettingsClick: () -> Unit = {},
    onNavigateToAgregar: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTab by remember { mutableStateOf(0) }
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var articuloEditando by remember { mutableStateOf<ArticuloCompra?>(null) }
    var dialogKey by remember { mutableIntStateOf(0) }

    var mostrarDialogoMoverADespensa by remember { mutableStateOf<ArticuloCompra?>(null) }

    LaunchedEffect(Unit) {
        viewModel.cargarArticulos()
    }

    LaunchedEffect(viewModel.mensajeOperacion) {
        viewModel.mensajeOperacion?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeOperacion()
        }
    }

    mostrarDialogoMoverADespensa?.let { articulo ->
        AlertDialog(
            onDismissRequest = { mostrarDialogoMoverADespensa = null },
            title = { Text("¿Mover a la despensa?", fontWeight = FontWeight.Bold) },
            text = { Text("Has marcado '${articulo.nombre}' como comprado. ¿Deseas agregarlo a tu inventario de despensa?") },
            confirmButton = {
                Button(
                    onClick = {
                        val nombre = articulo.nombre
                        val cantidad = articulo.cantidadEsperada?.toString() ?: ""
                        val unidad = articulo.unidad ?: ""
                        onNavigateToAgregar(nombre, cantidad, unidad)
                        mostrarDialogoMoverADespensa = null
                    }
                ) {
                    Text("Sí, mover")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoMoverADespensa = null }) {
                    Text("No, solo marcar")
                }
            }
        )
    }

    val edit = articuloEditando
    if (mostrarDialogoNuevo || edit != null) {
        val esNuevo = mostrarDialogoNuevo
        val cantidadInicial = if (!esNuevo && edit != null && edit.cantidadEsperada != null) {
            val c = edit.cantidadEsperada
            if (c == c.toLong().toDouble()) c.toLong().toString() else c.toString()
        } else ""
        
        var nombre by remember(dialogKey) { mutableStateOf(if (esNuevo) "" else edit?.nombre ?: "") }
        var cantidad by remember(dialogKey) { mutableStateOf(cantidadInicial) }
        val unidades = listOf("Kg", "Gramos", "Litros", "Piezas")
        var unidadSeleccionada by remember(dialogKey) { mutableStateOf(if (esNuevo) unidades[0] else edit?.unidad ?: unidades[0]) }
        var unidadExpanded by remember { mutableStateOf(false) }
        var errorNombre by remember(dialogKey) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { mostrarDialogoNuevo = false; articuloEditando = null },
            title = { Text(if (esNuevo) "Agregar a la lista" else "Editar artículo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cantidad,
                            onValueChange = { cantidad = it },
                            label = { Text("Cantidad") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = unidadExpanded,
                            onExpandedChange = { unidadExpanded = !unidadExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = unidadSeleccionada,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unidad") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unidadExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = unidadExpanded,
                                onDismissRequest = { unidadExpanded = false }
                            ) {
                                unidades.forEach { unidad ->
                                    DropdownMenuItem(
                                        text = { Text(unidad) },
                                        onClick = {
                                            unidadSeleccionada = unidad
                                            unidadExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombre.isBlank()) {
                            errorNombre = true
                        } else if (esNuevo) {
                            viewModel.agregarArticulo(nombre, cantidad, unidadSeleccionada)
                            mostrarDialogoNuevo = false
                        } else {
                            edit?.id?.let { viewModel.editarArticulo(it, nombre, cantidad, unidadSeleccionada) }
                            articuloEditando = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (esNuevo) "Agregar" else "Guardar", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevo = false; articuloEditando = null }) {
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
                    onClick = { mostrarDialogoNuevo = true; dialogKey++ },
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
        val haptic = LocalHapticFeedback.current

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    ShimmerComprasList(count = 6, modifier = Modifier.weight(1f))
                }
                is ListaComprasUiState.Error -> {
                    val errorDetails = ErrorUtils.getErrorDetails(context, uiState.throwable)
                    com.smart.comida.ui.components.ErrorState(
                        title = errorDetails.title,
                        message = uiState.message.ifBlank { errorDetails.message },
                        onRetry = { viewModel.cargarArticulos() },
                        modifier = Modifier.weight(1f)
                    )
                }
                is ListaComprasUiState.Success -> {
                    val todos = uiState.articulos
                    val articulos = if (selectedTab == 0) todos.filter { it.estado != "Confirmado" } else todos.filter { it.estado == "Confirmado" }

                    if (articulos.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.ShoppingCart,
                            title = if (selectedTab == 0) "Tu lista de compras está vacía" else "No hay compras confirmadas",
                            description = if (selectedTab == 0) "Agrega productos para empezar a planificar tus compras" else "Marca artículos como comprados y confirma la compra para verlos aquí",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        if (selectedTab == 0) {
                            val pendientes = todos.filter { it.estado != "Confirmado" }
                            val compradosPendientes = pendientes.count { it.estado == "Comprado" }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Progreso", fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
                                Text("$compradosPendientes de ${pendientes.size}", fontSize = 14.sp, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            }
                            LinearProgressIndicator(
                                progress = { if (pendientes.isEmpty()) 0f else compradosPendientes.toFloat() / pendientes.size.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = colorScheme.primary,
                                trackColor = colorScheme.primaryContainer
                            )

                            val comprados = todos.filter { it.estado == "Comprado" }
                            if (comprados.isNotEmpty()) {
                                Button(
                                    onClick = { viewModel.confirmarCompra() },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                                ) {
                                    Text("Confirmar compra (${comprados.size})", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(articulos, key = { it.id ?: it.hashCode() }) { articulo ->
                                val isChecked = articulo.estado == "Comprado" || articulo.estado == "Confirmado"
                                val textDecoration = if (isChecked) TextDecoration.LineThrough else null
                                val textColor = if (isChecked) colorScheme.onSurfaceVariant else colorScheme.onSurface

                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        when (value) {
                                            SwipeToDismissBoxValue.EndToStart -> {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                articulo.id?.let { viewModel.eliminarArticulo(it) }
                                                false
                                            }
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (articulo.estado != "Comprado" && articulo.estado != "Confirmado") {
                                                    mostrarDialogoMoverADespensa = articulo
                                                }
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
                                                .clickable { articuloEditando = articulo; dialogKey++ }
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
