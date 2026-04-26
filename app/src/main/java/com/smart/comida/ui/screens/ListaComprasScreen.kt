package com.smart.comida.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.data.model.ArticuloCompra
import com.smart.comida.ui.viewmodel.ListaComprasUiState
import com.smart.comida.ui.viewmodel.ListaComprasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaComprasScreen(
    viewModel: ListaComprasViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
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
            title = { Text("Agregar a la lista") },
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorNombre) {
                        Text("El nombre es obligatorio", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad esperada (Ej: 2 kg, 1 caja)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nombre.isBlank()) {
                            errorNombre = true
                        } else {
                            viewModel.agregarArticulo(nombre, cantidad)
                            mostrarDialogo = false
                        }
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = DarkCardBackground,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Lista de Compras", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = LightYellow,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar artículo")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is ListaComprasUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = LightYellow
                    )
                }
                is ListaComprasUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ListaComprasUiState.Success -> {
                    val articulos = uiState.articulos
                    if (articulos.isEmpty()) {
                        Text(
                            "Tu lista de compras está vacía.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(articulos) { articulo ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = articulo.nombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            if (articulo.cantidadEsperada.isNotBlank()) {
                                                Text(text = "Cantidad: ${articulo.cantidadEsperada}", color = Color.Gray, fontSize = 14.sp)
                                            }
                                        }
                                        IconButton(onClick = { articulo.id?.let { viewModel.eliminarArticulo(it) } }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
