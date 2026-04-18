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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteScreen(
    viewModel: IngredienteViewModel = viewModel(),
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

    // Cargar categorías al abrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarCategorias()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agregar Ingrediente") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del ingrediente *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Categoría
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
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
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

            // Fila Cantidad y Unidad
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
                    singleLine = true
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
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
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

            // Fecha de Caducidad con Interacción
            OutlinedTextField(
                value = fechaCaducidad,
                onValueChange = { },
                readOnly = true, // Evita que se escriba texto
                label = { Text("Fecha de Caducidad") },
                modifier = Modifier
                    .fillMaxWidth()
                    // Abre el calendario al tocar el campo
                    .clickable { mostrarCalendario = true },
                enabled = false, // Lo desactivamos visualmente como input
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Cuadro de Diálogo del Calendario
            if (mostrarCalendario) {
                DatePickerDialog(
                    onDismissRequest = { mostrarCalendario = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // Formatear los milisegundos a YYYY-MM-DD
                                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                fechaCaducidad = formatter.format(Date(millis))
                            }
                            mostrarCalendario = false
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is IngredienteUiState.Loading -> CircularProgressIndicator()
                is IngredienteUiState.Error -> Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                is IngredienteUiState.Success -> {
                    Text(text = "¡Ingrediente guardado!", color = MaterialTheme.colorScheme.primary)
                    LaunchedEffect(Unit) {
                        onGuardadoExitoso()
                        viewModel.resetState()
                    }
                }
                is IngredienteUiState.Idle -> { }
            }

            Button(
                onClick = {
                    viewModel.guardarIngrediente(
                        nombre = nombre,
                        cantidadStr = cantidad,
                        unidad = unidad,
                        fechaCaducidad = fechaCaducidad,
                        categoriaId = categoriaSeleccionada?.id // Enviamos el ID de la categoría
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is IngredienteUiState.Loading
            ) {
                Text("Guardar Ingrediente")
            }
        }
    }
}