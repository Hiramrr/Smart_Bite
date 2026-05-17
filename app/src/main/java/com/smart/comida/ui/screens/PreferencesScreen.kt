package com.smart.comida.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smart.comida.ui.viewmodel.PreferencesViewModel

val availableDiets = listOf(
    "gluten free" to "Sin Gluten",
    "ketogenic" to "Keto (Cetogénica)",
    "vegetarian" to "Vegetariana",
    "lacto-vegetarian" to "Lacto-Vegetariana",
    "ovo-vegetarian" to "Ovo-Vegetariana",
    "vegan" to "Vegana",
    "pescetarian" to "Pescetariana",
    "paleo" to "Paleo",
    "primal" to "Primal",
    "low fodmap" to "Baja en FODMAP",
    "whole30" to "Whole30"
)

val availableIntolerances = listOf(
    "dairy" to "Lácteos",
    "egg" to "Huevo",
    "gluten" to "Gluten",
    "grain" to "Granos",
    "peanut" to "Cacahuate / Maní",
    "seafood" to "Pescado",
    "sesame" to "Ajonjolí / Sésamo",
    "shellfish" to "Mariscos",
    "soy" to "Soya",
    "sulfite" to "Sulfitos",
    "tree nut" to "Frutos Secos (Nueces)",
    "wheat" to "Trigo"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var localDiets by remember { mutableStateOf(setOf<String>()) }
    var localIntolerances by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(uiState.preferences) {
        uiState.preferences?.let { prefs ->
            localDiets = prefs.diets.toSet()
            localIntolerances = prefs.intolerances.toSet()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Preferencias Dietéticas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.preferences == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text(
                            text = "Tipos de Dieta",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // Desestructuramos el Pair: apiValue es inglés, displayName es español
                    items(availableDiets) { (apiValue, displayName) ->
                        PreferenceRow(
                            label = displayName, // Mostramos español
                            isChecked = localDiets.contains(apiValue),
                            onCheckedChange = { isChecked ->
                                // Guardamos inglés en el estado
                                localDiets = if (isChecked) localDiets + apiValue else localDiets - apiValue
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Alergias e Intolerancias",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(availableIntolerances) { (apiValue, displayName) ->
                        PreferenceRow(
                            label = displayName, // Mostramos español
                            isChecked = localIntolerances.contains(apiValue),
                            onCheckedChange = { isChecked ->
                                // Guardamos inglés en el estado
                                localIntolerances = if (isChecked) localIntolerances + apiValue else localIntolerances - apiValue
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.savePreferences(
                            selectedDiets = localDiets.toList(),
                            selectedIntolerances = localIntolerances.toList()
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}