package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
    viewModel: ListaComprasViewModel = viewModel()
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
            title = { Text("Agregar a la lista", fontWeight = FontWeight.Bold, color = TextDark) },
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
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = GrayBorder,
                            focusedLabelColor = PrimaryGreen
                        )
                    )
                    if (errorNombre) {
                        Text("El nombre es obligatorio", color = RedExpiring, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad esperada (Ej: 2 kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = GrayBorder,
                            focusedLabelColor = PrimaryGreen
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
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Agregar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = TextGray)
                }
            },
            containerColor = CardWhite
        )
    }

    Scaffold(
        containerColor = BackgroundWhite,
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDark,
                    actionIconContentColor = TextDark
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
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
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
                    .background(Color(0xFFEFEFEF), RoundedCornerShape(24.dp))
                    .padding(4.dp)
            ) {
                TabButton(text = "Actual", isSelected = selectedTab == 0, modifier = Modifier.weight(1f)) { selectedTab = 0 }
                TabButton(text = "Historial", isSelected = selectedTab == 1, modifier = Modifier.weight(1f)) { selectedTab = 1 }
            }

            when (uiState) {
                is ListaComprasUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is ListaComprasUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiState.message, color = RedExpiring)
                    }
                }
                is ListaComprasUiState.Success -> {
                    val articulos = uiState.articulos
                    
                    if (articulos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(bottom = 56.dp), contentAlignment = Alignment.Center) {
                            Text("Tu lista de compras está vacía.", color = TextGray)
                        }
                    } else {
                        // Progress
                        val comprados = 0 // Mocked for now or add a boolean logic if DB supports it.
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Progreso", fontSize = 14.sp, color = TextGray)
                            Text("$comprados de ${articulos.size}", fontSize = 14.sp, color = TextGray, fontWeight = FontWeight.Medium)
                        }
                        LinearProgressIndicator(
                            progress = { if (articulos.isEmpty()) 0f else comprados.toFloat() / articulos.size.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = PrimaryGreen,
                            trackColor = LightGreen
                        )

                        // List
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(articulos) { articulo ->
                                val isChecked = false // Check if database supports 'comprado' boolean. If not, it's just a UI interaction mocking.
                                val textDecoration = if (isChecked) TextDecoration.LineThrough else null
                                val textColor = if (isChecked) TextGray else TextDark
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isChecked,
                                        onClick = { /* TODO toggle */ },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = PrimaryGreen,
                                            unselectedColor = GrayBorder
                                        )
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(articulo.nombre, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Medium, textDecoration = textDecoration)
                                        articulo.cantidadEsperada?.let {
                                            val displayCantidad = if (articulo.unidad != null) "$it ${articulo.unidad}" else it.toString()
                                            Text(displayCantidad, fontSize = 12.sp, color = TextGray, textDecoration = textDecoration)
                                        }
                                    }
                                    IconButton(onClick = { articulo.id?.let { viewModel.eliminarArticulo(it) } }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextGray)
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFEFEFEF), modifier = Modifier.padding(start = 48.dp))
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
    val bgColor = if (isSelected) PrimaryGreen else Color.Transparent
    val textColor = if (isSelected) Color.White else TextGray
    
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
