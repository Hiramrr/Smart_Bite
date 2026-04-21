package com.smart.comida.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.data.model.Categoria
import com.smart.comida.ui.viewmodel.IngredienteUiState
import com.smart.comida.ui.viewmodel.IngredienteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteScreen(
    viewModel: IngredienteViewModel = viewModel(),
    onVolver: () -> Unit = {},
    onGuardadoExitoso: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var fechaCaducidad by remember { mutableStateOf("") }

    // Categorías
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }
    var expandirCategoria by remember { mutableStateOf(false) }

    // Calendario
    var mostrarCalendario by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var expandirDropdownUnidad by remember { mutableStateOf(false) }
    val opcionesUnidad = listOf("kg", "litros", "piezas", "gramos")

    val uiState = viewModel.uiState

    // --- VARIABLES PARA LA FOTO ---
    val context = LocalContext.current
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzador para abrir la galería de Android
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imagenUri = uri }
    )

    // Cargar categorías al abrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarCategorias()
    }

    val formColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = LightYellow,
        unfocusedBorderColor = Color(0xFF5A5A5C),
        focusedContainerColor = DarkCardBackground,
        unfocusedContainerColor = DarkCardBackground,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = LightYellow,
        unfocusedLabelColor = Color(0xFFB3B3B3),
        cursorColor = LightYellow,
        disabledContainerColor = DarkCardBackground,
        disabledTextColor = Color.White,
        disabledBorderColor = Color(0xFF5A5A5C),
        disabledLabelColor = Color(0xFFB3B3B3)
    )

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Agregar ingrediente", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Agrega un nuevo ingrediente a tu despensa",
                color = Color(0xFFB3B3B3),
                fontSize = 14.sp
            )

            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCardBackground)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imagenUri != null) {
                    AsyncImage(
                        model = imagenUri,
                        contentDescription = "Foto seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar foto",
                        tint = Color(0xFFB3B3B3),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del ingrediente *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = formColors
            )

            ExposedDropdownMenuBox(
                expanded = expandirCategoria,
                onExpandedChange = { expandirCategoria = !expandirCategoria },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = categoriaSeleccionada?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría (Opcional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirCategoria) },
                    colors = formColors,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandirCategoria,
                    onDismissRequest = { expandirCategoria = false }
                ) {
                    viewModel.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                categoriaSeleccionada = categoria
                                expandirCategoria = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = formColors
                )

                ExposedDropdownMenuBox(
                    expanded = expandirDropdownUnidad,
                    onExpandedChange = { expandirDropdownUnidad = !expandirDropdownUnidad },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unidad,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirDropdownUnidad) },
                        colors = formColors,
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandirDropdownUnidad,
                        onDismissRequest = { expandirDropdownUnidad = false }
                    ) {
                        opcionesUnidad.forEach { seleccion ->
                            DropdownMenuItem(
                                text = { Text(seleccion) },
                                onClick = {
                                    unidad = seleccion
                                    expandirDropdownUnidad = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = fechaCaducidad,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha de Caducidad") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarCalendario = true },
                enabled = false,
                shape = RoundedCornerShape(16.dp),
                colors = formColors
            )

            if (mostrarCalendario) {
                DatePickerDialog(
                    onDismissRequest = { mostrarCalendario = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                                formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")

                                fechaCaducidad = formatter.format(Date(millis))
                                if (uiState is IngredienteUiState.Error) viewModel.resetState()
                            }
                            mostrarCalendario = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
                    }
                ){
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is IngredienteUiState.Loading -> CircularProgressIndicator(color = LightYellow)
                is IngredienteUiState.Error -> Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                is IngredienteUiState.Success -> {
                    LaunchedEffect(Unit) {
                        onGuardadoExitoso()
                        viewModel.resetState()
                    }
                }
                is IngredienteUiState.Idle -> { }
            }

            Button(
                onClick = {
                    val bytesDeImagen = imagenUri?.let { uri ->
                        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    }

                    viewModel.guardarIngrediente(
                        nombre = nombre,
                        cantidadStr = cantidad,
                        unidad = unidad,
                        fechaCaducidad = fechaCaducidad,
                        categoriaId = categoriaSeleccionada?.id,
                        imagenBytes = bytesDeImagen
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = LightYellow.copy(alpha = 0.5f)
                ),
                enabled = uiState !is IngredienteUiState.Loading
            ) {
                Text("Guardar ingrediente", fontWeight = FontWeight.Bold)
            }
        }
    }
}
