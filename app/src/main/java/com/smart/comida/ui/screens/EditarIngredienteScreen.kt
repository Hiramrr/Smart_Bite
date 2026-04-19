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
import com.smart.comida.ui.viewmodel.EditarIngredienteViewModel
import com.smart.comida.ui.viewmodel.IngredienteUiState
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarIngredienteScreen(
    ingredienteId: Int, // Recibimos el ID a editar
    viewModel: EditarIngredienteViewModel = viewModel(),
    onGuardadoExitoso: () -> Unit
) {
    var expandirCategoria by remember { mutableStateOf(false) }
    var mostrarCalendario by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var expandirDropdownUnidad by remember { mutableStateOf(false) }
    val opcionesUnidad = listOf("kg", "litros", "piezas", "gramos")

    val context = LocalContext.current
    var imagenUri by remember { mutableStateOf<Uri?>(null) } // La NUEVA foto seleccionada

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imagenUri = uri }
    )

    val uiState = viewModel.uiState

    // Al abrir la pantalla, le decimos al ViewModel que descargue los datos
    LaunchedEffect(ingredienteId) {
        viewModel.cargarDatos(ingredienteId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Editar Ingrediente") }) }
    ) { paddingValues ->
        if (uiState is IngredienteUiState.Loading && viewModel.nombre.isEmpty()) {
            // Si está cargando al inicio, mostramos un círculo en el centro
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imagenUri != null) {
                        // Si seleccionó una NUEVA foto, se muestra esta temporalmente
                        AsyncImage(
                            model = imagenUri, contentDescription = "Nueva Foto",
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                    } else if (!viewModel.imagenUrl.isNullOrEmpty()) {
                        // Si no ha tocado nada, mostramos la foto VIEJA que ya está en la nube
                        AsyncImage(
                            model = viewModel.imagenUrl, contentDescription = "Foto Actual",
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                    } else {
                        // Si de plano no tiene foto nunca
                        Icon(
                            imageVector = Icons.Default.Add, contentDescription = "Agregar foto",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp)
                        )
                    }
                }
                // Fíjate cómo ahora leemos y escribimos directamente en viewModel.nombre
                OutlinedTextField(
                    value = viewModel.nombre,
                    onValueChange = { viewModel.nombre = it },
                    label = { Text("Nombre del ingrediente *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expandirCategoria,
                    onExpandedChange = { expandirCategoria = !expandirCategoria },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = viewModel.categoriaSeleccionada?.nombre ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Categoría (Opcional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirCategoria) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandirCategoria, onDismissRequest = { expandirCategoria = false }
                    ) {
                        viewModel.categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria.nombre) },
                                onClick = {
                                    viewModel.categoriaSeleccionada = categoria
                                    expandirCategoria = false
                                }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = viewModel.cantidad,
                        onValueChange = {
                            viewModel.cantidad = it
                            // Si había un error previo, lo limpiamos al empezar a escribir de nuevo
                            if (uiState is IngredienteUiState.Error) viewModel.resetState()
                        },
                        label = { Text("Cantidad *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        // Opcional: Pone el contorno en rojo si el error es de cantidad
                        isError = uiState is IngredienteUiState.Error && (viewModel.cantidad.toFloatOrNull() ?: -1f) < 0,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandirDropdownUnidad,
                        onExpandedChange = { expandirDropdownUnidad = !expandirDropdownUnidad },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = viewModel.unidad, onValueChange = {}, readOnly = true, label = { Text("Unidad *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirDropdownUnidad) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandirDropdownUnidad, onDismissRequest = { expandirDropdownUnidad = false }
                        ) {
                            opcionesUnidad.forEach { seleccion ->
                                DropdownMenuItem(
                                    text = { Text(seleccion) },
                                    onClick = { viewModel.unidad = seleccion; expandirDropdownUnidad = false }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.fechaCaducidad, onValueChange = { }, readOnly = true,
                    label = { Text("Fecha de Caducidad") },
                    modifier = Modifier.fillMaxWidth().clickable { mostrarCalendario = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                if (mostrarCalendario) {
                    DatePickerDialog(
                        onDismissRequest = { mostrarCalendario = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                                    // ¡esta linea arregla el problema de la fecha, no la borren
                                    formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")

                                    viewModel.fechaCaducidad = formatter.format(Date(millis))
                                }
                                mostrarCalendario = false
                            }) { Text("Aceptar") }
                        },
                        dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }
                    ) { DatePicker(state = datePickerState) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (uiState) {
                    is IngredienteUiState.Loading -> CircularProgressIndicator()
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
                        // Extraemos los bytes de la foto NUEVA (si es que seleccionó una)
                        val bytesDeImagen = imagenUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        }
                        viewModel.guardarCambios(id = ingredienteId, imagenBytes = bytesDeImagen)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is IngredienteUiState.Loading
                ) {
                    Text("Actualizar Ingrediente")
                }
            }
        }
    }
}