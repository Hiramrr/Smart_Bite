package com.smart.comida.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.ui.viewmodel.DespensaViewModel
import com.smart.comida.ui.viewmodel.RecipeUiState
import com.smart.comida.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleIngredienteScreen(
    ingredienteId: Int,
    onVolver: () -> Unit,
    onVerRecetaClick: (Int) -> Unit,
    despensaViewModel: DespensaViewModel,
    recipeViewModel: RecipeViewModel = viewModel()
){
    // Observamos los estados
    val ingrediente = despensaViewModel.uiState.let { state ->
        if (state is com.smart.comida.ui.viewmodel.DespensaUiState.Success) {
            state.ingredientes.find { it.id == ingredienteId }
        } else null
    }

    val recipeState by recipeViewModel.uiState.collectAsState()

    // Cuando la pantalla se abre y tenemos el ingrediente, buscamos recetas en tu backend
    LaunchedEffect(ingrediente) {
        if (ingrediente != null) {
            recipeViewModel.searchRecipes(ingrediente.nombre)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ingrediente?.nombre ?: "Detalles") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // 1. Info del Ingrediente
            ingrediente?.let { ing ->
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!ing.imagenUrl.isNullOrEmpty()) {
                        AsyncImage(model = ing.imagenUrl, contentDescription = null, modifier = Modifier.size(120.dp))
                    }
                    Text("Cantidad disponible: ${ing.cantidad} ${ing.unidad ?: ""}", style = MaterialTheme.typography.titleMedium)
                    Text("Caduca: ${ing.fechaCaducidad ?: "S/F"}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Divider()

            // 2. Título de Recetas Sugeridas
            Text(
                "¿Qué preparar con esto?",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            // 3. Lista de Recetas desde tu Backend (CU-08)
            when (recipeState) {
                is RecipeUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is RecipeUiState.Error -> Text((recipeState as RecipeUiState.Error).message, color = MaterialTheme.colorScheme.error)
                is RecipeUiState.SearchSuccess -> {
                    val recetas = (recipeState as RecipeUiState.SearchSuccess).recipes
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(recetas) { receta ->
                            ListItem(
                                modifier = Modifier.clickable { onVerRecetaClick(receta.id) },
                                headlineContent = { Text(receta.title) },
                                leadingContent = {
                                    AsyncImage(model = receta.image, contentDescription = null, modifier = Modifier.size(56.dp))
                                }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}