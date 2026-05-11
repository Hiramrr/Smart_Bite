package com.smart.comida.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.data.model.Categoria
import com.smart.comida.ui.components.EmptyState
import com.smart.comida.ui.components.ShimmerComprasList
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.CategoriaUiState
import com.smart.comida.ui.viewmodel.CategoriaViewModel
import com.smart.comida.util.ErrorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    viewModel: CategoriaViewModel = viewModel(),
    onVolver: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme

    var mostrarDialogo by remember { mutableStateOf(false) }
    var categoriaEditando by remember { mutableStateOf<Categoria?>(null) }
    var dialogKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.cargarCategorias()
    }

    LaunchedEffect(viewModel.mensajeOperacion) {
        viewModel.mensajeOperacion?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajeOperacion()
        }
    }

    val edit = categoriaEditando
    if (mostrarDialogo || edit != null) {
        val esNuevo = mostrarDialogo
        var nombre by remember(dialogKey) { mutableStateOf(if (esNuevo) "" else edit?.nombre ?: "") }
        var errorNombre by remember(dialogKey) { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { mostrarDialogo = false; categoriaEditando = null },
            title = {
                Text(
                    if (esNuevo) "Nueva categoría" else "Editar categoría",
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        if (it.isNotBlank()) errorNombre = false
                    },
                    label = { Text("Nombre de la categoría") },
                    isError = errorNombre,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline,
                        focusedLabelColor = colorScheme.primary
                    )
                )
                if (errorNombre) {
                    Text(
                        "El nombre es obligatorio",
                        color = colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nombre.isBlank()) {
                            errorNombre = true
                        } else if (esNuevo) {
                            viewModel.crearCategoria(nombre)
                            mostrarDialogo = false
                        } else {
                            edit?.id?.let { viewModel.actualizarCategoria(it, nombre) }
                            categoriaEditando = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text(if (esNuevo) "Crear" else "Guardar", color = colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false; categoriaEditando = null }) {
                    Text("Cancelar", color = colorScheme.onSurfaceVariant)
                }
            },
            containerColor = colorScheme.surface
        )
    }

    Scaffold(
        containerColor = colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Categorías", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Button(
                onClick = { mostrarDialogo = true; dialogKey++ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar categoría", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { paddingValues ->
        val haptic = LocalHapticFeedback.current

        when (val state = viewModel.uiState) {
            is CategoriaUiState.Loading -> {
                ShimmerComprasList(
                    count = 6,
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                )
            }
            is CategoriaUiState.Error -> {
                val errorDetails = ErrorUtils.getErrorDetails(context, state.throwable)
                com.smart.comida.ui.components.ErrorState(
                    title = errorDetails.title,
                    message = state.message.ifBlank { errorDetails.message },
                    onRetry = { viewModel.cargarCategorias() },
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                )
            }
            is CategoriaUiState.Success -> {
                val categorias = state.categorias

                if (categorias.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Category,
                        title = "No hay categorías",
                        description = "Crea categorías para organizar tus ingredientes",
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(categorias, key = { it.id ?: it.hashCode() }) { categoria ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.EndToStart) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        categoria.id?.let { viewModel.eliminarCategoria(it) }
                                        false
                                    }
                                    false
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    val colorAnim by animateColorAsState(
                                        when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.EndToStart -> colorScheme.error.copy(alpha = 0.8f)
                                            else -> Color.Transparent
                                        },
                                        label = "bgColor"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(colorAnim)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                },
                                content = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                categoriaEditando = categoria
                                                dialogKey++
                                            }
                                            .background(colorScheme.surface, RoundedCornerShape(16.dp))
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            categoria.nombre,
                                            fontSize = 16.sp,
                                            color = colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
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
