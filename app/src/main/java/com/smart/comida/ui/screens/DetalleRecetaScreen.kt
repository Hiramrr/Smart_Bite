package com.smart.comida.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.ui.viewmodel.RecipeUiState
import com.smart.comida.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRecetaScreen(
    recetaId: Int,
    onVolver: () -> Unit,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val uiState by recipeViewModel.uiState.collectAsState()

    // Buscamos los detalles de la receta al abrir la pantalla (CU-09)
    LaunchedEffect(recetaId) {
        recipeViewModel.getRecipeDetail(recetaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preparación") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is RecipeUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is RecipeUiState.Error -> Text((uiState as RecipeUiState.Error).message, modifier = Modifier.align(Alignment.Center))
                is RecipeUiState.DetailSuccess -> {
                    val receta = (uiState as RecipeUiState.DetailSuccess).recipe

                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        item {
                            AsyncImage(
                                model = receta.image,
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(receta.title, style = MaterialTheme.typography.headlineMedium)
                            Text("Tiempo: ${receta.readyInMinutes} min • Porciones: ${receta.servings}", color = MaterialTheme.colorScheme.primary)

                            Spacer(Modifier.height(16.dp))
                            Text("Ingredientes", style = MaterialTheme.typography.titleLarge)
                        }

                        // Imprimir ingredientes (El traductor iría aquí en el futuro)
                        items(receta.extendedIngredients) { ingrediente ->
                            Text("• ${ingrediente.original}", modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("Pasos", style = MaterialTheme.typography.titleLarge)
                        }

                        // Imprimir pasos (Spoonacular manda las instrucciones como una lista dentro de otra)
                        if (receta.analyzedInstructions.isNotEmpty()) {
                            items(receta.analyzedInstructions[0].steps) { paso ->
                                Text("${paso.number}. ${paso.step}", modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}