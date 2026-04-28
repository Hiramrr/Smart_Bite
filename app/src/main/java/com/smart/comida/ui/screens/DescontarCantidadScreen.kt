package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.DescontarUiState
import com.smart.comida.ui.viewmodel.DescontarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DescontarCantidadScreen(
    ingredienteId: Int,
    onVolver: () -> Unit,
    onDescontadoExitoso: () -> Unit,
    viewModel: DescontarViewModel = viewModel()
) {
    var cantidadADescontar by remember { mutableStateOf("") }
    var unidadSeleccionada by remember { mutableStateOf("") }
    var expandedUnidades by remember { mutableStateOf(false) }

    val ing = viewModel.ingrediente

    // Cargar ingrediente al iniciar
    LaunchedEffect(ingredienteId) {
        viewModel.cargarIngrediente(ingredienteId)
    }

    // Inicializar unidad por defecto cuando cargue el ingrediente
    LaunchedEffect(ing) {
        if (ing != null && unidadSeleccionada.isEmpty()) {
            unidadSeleccionada = ing.unidad ?: "Piezas"
        }
    }

    // Manejar éxito
    LaunchedEffect(viewModel.uiState) {
        if (viewModel.uiState is DescontarUiState.Success) {
            onDescontadoExitoso()
            viewModel.resetState()
        }
    }

    // Determinar opciones de unidades basadas en la unidad original
    val opcionesUnidades = when (ing?.unidad) {
        "Kg" -> listOf("Kg", "Gramos")
        "Litros" -> listOf("Litros", "ml")
        else -> listOf(ing?.unidad ?: "Piezas")
    }

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = { Text("Descontar cantidad", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDark,
                    navigationIconContentColor = TextDark
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (viewModel.uiState is DescontarUiState.Error) {
                    Text(
                        text = (viewModel.uiState as DescontarUiState.Error).message,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = { viewModel.descontarCantidad(cantidadADescontar, unidadSeleccionada) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp),
                    enabled = viewModel.uiState !is DescontarUiState.Loading && ing != null
                ) {
                    if (viewModel.uiState is DescontarUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Confirmar descuento", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (ing == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Info del ingrediente real
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!ing.imagenUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ing.imagenUrl,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(60.dp).background(LightGreen, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text(ing.nombre.take(1), fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(ing.nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextDark)
                        Text("${ing.cantidad} ${ing.unidad} disponibles", fontSize = 15.sp, color = TextGray)
                    }
                }

                Divider(color = GrayBorder.copy(alpha = 0.5f))

                Text("¿Cuánta cantidad vas a descontar?", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextDark)

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = cantidadADescontar,
                        onValueChange = { cantidadADescontar = it },
                        placeholder = { Text("0.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = GrayBorder,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedContainerColor = CardWhite,
                            focusedContainerColor = CardWhite
                        )
                    )
                    
                    // Dropdown de Unidades Inteligente
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = unidadSeleccionada,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                            modifier = Modifier.fillMaxWidth().clickable { expandedUnidades = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = GrayBorder,
                                focusedBorderColor = PrimaryGreen,
                                unfocusedContainerColor = CardWhite,
                                focusedContainerColor = CardWhite
                            )
                        )
                        DropdownMenu(
                            expanded = expandedUnidades,
                            onDismissRequest = { expandedUnidades = false }
                        ) {
                            opcionesUnidades.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unidadSeleccionada = u
                                        expandedUnidades = false
                                    }
                                )
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = LightGreen.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = PrimaryGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Se restará de tu inventario actual de forma manual.",
                            fontSize = 13.sp,
                            color = GreenAccent
                        )
                    }
                }
            }
        }
    }
}
