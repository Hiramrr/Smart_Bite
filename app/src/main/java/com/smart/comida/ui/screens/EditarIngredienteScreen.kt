package com.smart.comida.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.EditarIngredienteViewModel
import com.smart.comida.ui.viewmodel.IngredienteUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarIngredienteScreen(
    ingredienteId: Int,
    viewModel: EditarIngredienteViewModel = viewModel(),
    onVolver: () -> Unit = {},
    onGuardadoExitoso: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val uiState = viewModel.uiState

    // Estados para menús y diálogos
    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedUnidad by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val unidades = listOf("Kg", "Gramos", "Litros", "Piezas")

    // Cargar datos al iniciar
    LaunchedEffect(ingredienteId) {
        viewModel.cargarDatos(ingredienteId)
    }

    // Manejar éxito
    LaunchedEffect(uiState) {
        if (uiState is IngredienteUiState.Success) {
            onGuardadoExitoso()
            viewModel.resetState()
        }
    }

    // Launcher para imagen
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imagenUri = uri }

    // DatePicker State
    val datePickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        viewModel.fechaCaducidad = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = { Text("Editar ingrediente", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                if (uiState is IngredienteUiState.Error) {
                    Text(
                        text = uiState.message,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = {
                        val imageBytes = imagenUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }
                        viewModel.guardarCambios(id = ingredienteId, imagenBytes = imageBytes)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uiState !is IngredienteUiState.Loading
                ) {
                    if (uiState is IngredienteUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Actualizar ingrediente", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState is IngredienteUiState.Loading && viewModel.nombre.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image Picker
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(LightGreen)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imagenUri != null) {
                        AsyncImage(
                            model = imagenUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!viewModel.imagenUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = viewModel.imagenUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Image, null, tint = PrimaryGreen, modifier = Modifier.size(80.dp))
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-8).dp, y = (-8).dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = TextDark, modifier = Modifier.size(20.dp))
                    }
                }

                FormTextField(
                    label = "Nombre *",
                    value = viewModel.nombre,
                    onValueChange = { viewModel.nombre = it },
                    placeholder = "Ej. Aguacate"
                )

                // Selector de Categoría
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Categoría *", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoria,
                        onExpandedChange = { expandedCategoria = it }
                    ) {
                        OutlinedTextField(
                            value = viewModel.categoriaSeleccionada?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Seleccionar categoría", color = TextGray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = GrayBorder,
                                focusedBorderColor = PrimaryGreen,
                                unfocusedContainerColor = CardWhite,
                                focusedContainerColor = CardWhite
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategoria,
                            onDismissRequest = { expandedCategoria = false }
                        ) {
                            viewModel.categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nombre) },
                                    onClick = {
                                        viewModel.categoriaSeleccionada = cat
                                        expandedCategoria = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(
                            label = "Cantidad *",
                            value = viewModel.cantidad,
                            onValueChange = { viewModel.cantidad = it },
                            placeholder = "Ej. 2",
                            keyboardType = KeyboardType.Number
                        )
                    }
                    // Selector de Unidad
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Unidad *", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        ExposedDropdownMenuBox(
                            expanded = expandedUnidad,
                            onExpandedChange = { expandedUnidad = it }
                        ) {
                            OutlinedTextField(
                                value = viewModel.unidad,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Unidad", color = TextGray) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnidad) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = GrayBorder,
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedContainerColor = CardWhite,
                                    focusedContainerColor = CardWhite
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expandedUnidad,
                                onDismissRequest = { expandedUnidad = false }
                            ) {
                                unidades.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            viewModel.unidad = u
                                            expandedUnidad = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Selector de Fecha
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Fecha de caducidad", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    OutlinedTextField(
                        value = viewModel.fechaCaducidad,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Seleccionar fecha", color = TextGray) },
                        trailingIcon = { 
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, null, tint = TextGray)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = GrayBorder,
                            focusedBorderColor = PrimaryGreen,
                            unfocusedContainerColor = CardWhite,
                            focusedContainerColor = CardWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
