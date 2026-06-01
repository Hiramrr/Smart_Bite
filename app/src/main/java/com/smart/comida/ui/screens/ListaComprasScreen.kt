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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaComprasScreen(
    viewModel: ListaComprasViewModel = viewModel(),
    onSettingsClick: () -> Unit = {}
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedTab by remember { mutableStateOf(0) }
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var articuloEditando by remember { mutableStateOf<ArticuloCompra?>(null) }
    var articuloAEliminar by remember { mutableStateOf<ArticuloCompra?>(null) }
    var articuloCompradoParaDespensa by remember { mutableStateOf<ArticuloCompra?>(null) }
    var solicitarFechaDespensa by remember { mutableStateOf(false) }
    var fechaCaducidadCompra by remember { mutableStateOf("") }
    var fechaCompraError by remember { mutableStateOf(false) }
    var mostrarDatePickerCompra by remember { mutableStateOf(false) }
    var mostrarDialogoLimpiarLista by remember { mutableStateOf(false) }
    var dialogKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.cargarArticulos()
    }

    LaunchedEffect(viewModel.mensajeOperacion) {
        viewModel.mensajeOperacion?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeOperacion()
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    articuloAEliminar?.let { articulo ->
        AlertDialog(
            onDismissRequest = { articuloAEliminar = null },
            title = { Text("Eliminar artículo") },
            text = { Text("¿Deseas quitar ${articulo.nombre} de la lista de compras?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        articulo.id?.let(viewModel::eliminarArticulo)
                        articuloAEliminar = null
                    }
                ) {
                    Text("Eliminar", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { articuloAEliminar = null }) {
                    Text("Cancelar")
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
                            if (viewModel.agregarArticulo(nombre, cantidad, unidadSeleccionada)) {
                                mostrarDialogoNuevo = false
                            }
                        } else {
                            edit?.id?.let {
                                if (viewModel.editarArticulo(it, nombre, cantidad, unidadSeleccionada)) {
                                    articuloEditando = null
                                }
                            }
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

    val datePickerState = rememberDatePickerState()
    if (mostrarDatePickerCompra) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePickerCompra = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            fechaCaducidadCompra = formato.format(Date(selectedMillis))
                            fechaCompraError = !fechaCaducidadValida(fechaCaducidadCompra)
                        }
                        mostrarDatePickerCompra = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePickerCompra = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val articuloParaDespensa = articuloCompradoParaDespensa
    if (articuloParaDespensa != null && !solicitarFechaDespensa) {
        AlertDialog(
            onDismissRequest = { articuloCompradoParaDespensa = null },
            title = { Text("Producto comprado", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Deseas mover ${articuloParaDespensa.nombre} a tu despensa?")
            },
            confirmButton = {
                Button(onClick = { solicitarFechaDespensa = true }) {
                    Text("Mover a despensa")
                }
            },
            dismissButton = {
                TextButton(onClick = { articuloCompradoParaDespensa = null }) {
                    Text("Solo marcar comprado")
                }
            }
        )
    }

    if (articuloParaDespensa != null && solicitarFechaDespensa) {
        AlertDialog(
            onDismissRequest = {
                articuloCompradoParaDespensa = null
                solicitarFechaDespensa = false
                fechaCaducidadCompra = ""
                fechaCompraError = false
            },
            title = { Text("Fecha de caducidad", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Selecciona la fecha de caducidad de ${articuloParaDespensa.nombre}.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = fechaCaducidadCompra,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de caducidad") },
                        placeholder = { Text("Seleccionar fecha") },
                        isError = fechaCompraError,
                        trailingIcon = {
                            IconButton(onClick = { mostrarDatePickerCompra = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { mostrarDatePickerCompra = true }
                    )
                    if (fechaCompraError) {
                        Text("Ingresa una fecha válida igual o posterior a hoy.", color = colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        fechaCompraError = !fechaCaducidadValida(fechaCaducidadCompra)
                        if (fechaCompraError) return@Button
                        viewModel.moverArticuloCompradoADespensa(articuloParaDespensa, fechaCaducidadCompra)
                        articuloCompradoParaDespensa = null
                        solicitarFechaDespensa = false
                        fechaCaducidadCompra = ""
                    }
                ) {
                    Text("Guardar en despensa")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        articuloCompradoParaDespensa = null
                        solicitarFechaDespensa = false
                        fechaCaducidadCompra = ""
                        fechaCompraError = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoLimpiarLista) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLimpiarLista = false },
            title = { Text("Limpiar lista") },
            text = { Text("¿Deseas eliminar todos los artículos de la lista de compras? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarDialogoLimpiarLista = false
                        viewModel.limpiarLista()
                    }
                ) {
                    Text("Limpiar lista", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoLimpiarLista = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    fun alternarEstadoComprado(articulo: ArticuloCompra) {
        articulo.id?.let { id ->
            viewModel.marcarComoComprado(id, articulo.estado) { nuevoEstado ->
                if (nuevoEstado == "Comprado") {
                    articuloCompradoParaDespensa = articulo
                    solicitarFechaDespensa = false
                    fechaCaducidadCompra = ""
                    fechaCompraError = false
                }
            }
        }
    }

    val hayArticulosEnLista = (uiState as? ListaComprasUiState.Success)?.articulos?.isNotEmpty() == true

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Lista de compras", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    if (hayArticulosEnLista) {
                        IconButton(onClick = { mostrarDialogoLimpiarLista = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar lista")
                        }
                    }
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
                            actionLabel = if (selectedTab == 0) "Agregar producto" else null,
                            onActionClick = if (selectedTab == 0) {
                                {
                                    mostrarDialogoNuevo = true
                                    dialogKey++
                                }
                            } else {
                                null
                            },
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

                            OutlinedButton(
                                onClick = { mostrarDialogoLimpiarLista = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.error)
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Limpiar lista", fontWeight = FontWeight.Bold)
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
                                                articuloAEliminar = articulo
                                                false
                                            }
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                alternarEstadoComprado(articulo)
                                                false
                                            }
                                            SwipeToDismissBoxValue.Settled -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = articulo.estado != "Confirmado",
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
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { alternarEstadoComprado(articulo) },
                                                enabled = articulo.estado != "Confirmado"
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(articulo.nombre, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Medium, textDecoration = textDecoration)
                                                articulo.cantidadEsperada?.let {
                                                    val displayCantidad = if (articulo.unidad != null) "$it ${articulo.unidad}" else it.toString()
                                                    Text(displayCantidad, fontSize = 12.sp, color = colorScheme.onSurfaceVariant, textDecoration = textDecoration)
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    articuloEditando = articulo
                                                    dialogKey++
                                                }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar artículo")
                                            }
                                            IconButton(onClick = { articuloAEliminar = articulo }) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Eliminar artículo",
                                                    tint = colorScheme.error
                                                )
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

private fun fechaCaducidadValida(fecha: String): Boolean {
    return try {
        val fechaSeleccionada = LocalDate.parse(fecha)
        !fechaSeleccionada.isBefore(LocalDate.now())
    } catch (e: DateTimeParseException) {
        false
    }
}
