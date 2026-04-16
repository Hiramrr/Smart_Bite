package com.smart.comida.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.ui.viewmodel.IngredienteUiState
import com.smart.comida.ui.viewmodel.IngredienteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteScreen(
    // Inyectamos el ViewModel
    viewModel: IngredienteViewModel = viewModel(),
    // Función para navegar cuando se guarde con éxito (opcional por ahora)
    onGuardadoExitoso: () -> Unit = {}
) {
    // Variables de estado para los campos de texto
    var nombre by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var fechaCaducidad by remember { mutableStateOf("") } // Formato YYYY-MM-DD

    // Estado del Dropdown de Unidades
    var expandirDropdown by remember { mutableStateOf(false) }
    val opcionesUnidad = listOf("kg", "litros", "piezas", "gramos")

    // Observamos el estado del ViewModel
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Ingrediente") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
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
            // Campo: Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del ingrediente *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Fila para Cantidad y Unidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo: Cantidad
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Selector: Unidad
                ExposedDropdownMenuBox(
                    expanded = expandirDropdown,
                    onExpandedChange = { expandirDropdown = !expandirDropdown },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unidad,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unidad *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirDropdown) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandirDropdown,
                        onDismissRequest = { expandirDropdown = false }
                    ) {
                        opcionesUnidad.forEach { seleccion ->
                            DropdownMenuItem(
                                text = { Text(seleccion) },
                                onClick = {
                                    unidad = seleccion
                                    expandirDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Campo: Fecha de Caducidad (Por ahora un campo de texto simple)
            OutlinedTextField(
                value = fechaCaducidad,
                onValueChange = { fechaCaducidad = it },
                label = { Text("Fecha de Caducidad (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ej: 2024-12-31") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Manejo de Estados de la UI (Carga, Éxito, Error)
            when (uiState) {
                is IngredienteUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is IngredienteUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is IngredienteUiState.Success -> {
                    Text(
                        text = "¡Ingrediente guardado con éxito!",
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Disparamos la acción de éxito (como limpiar campos o navegar)
                    LaunchedEffect(Unit) {
                        onGuardadoExitoso()
                        viewModel.resetState() // Volvemos al estado inicial
                        nombre = ""
                        cantidad = ""
                        unidad = ""
                        fechaCaducidad = ""
                    }
                }
                is IngredienteUiState.Idle -> { /* Sin acción */ }
            }

            // Botón Guardar
            Button(
                onClick = {
                    viewModel.guardarIngrediente(
                        nombre = nombre,
                        cantidadStr = cantidad,
                        unidad = unidad,
                        fechaCaducidad = fechaCaducidad
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is IngredienteUiState.Loading // Deshabilitar mientras carga
            ) {
                Text("Guardar Ingrediente")
            }
        }
    }
}