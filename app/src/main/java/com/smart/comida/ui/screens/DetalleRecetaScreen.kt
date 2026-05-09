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
import androidx.compose.ui.platform.LocalContext
import com.smart.comida.util.ErrorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.gson.Gson // Importación de Gson para serializar
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import com.smart.comida.presentation.components.FavoriteToggleButton
import com.smart.comida.presentation.viewmodel.RecipeDetailViewModel
import com.smart.comida.presentation.viewmodel.RecipeDetailViewModelFactory
import com.smart.comida.ui.viewmodel.RecipeUiState
import com.smart.comida.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRecetaScreen(
    recetaId: Int,
    onVolver: () -> Unit,
    recipeViewModel: RecipeViewModel = viewModel(),
    favoritesRepository: FavoritesRepository // Inyectado desde tu NavHost
) {
    // 1. Estado de red (Spoonacular)
    val uiState by recipeViewModel.uiState.collectAsState()

    // 2. Instanciación del ViewModel Local (Room) usando la Factoría
    val favoriteViewModel: RecipeDetailViewModel = viewModel(
        factory = RecipeDetailViewModelFactory(favoritesRepository, recetaId)
    )

    // 3. Estado reactivo de la BD local (Flow -> State)
    val isFavorite by favoriteViewModel.isFavorite.collectAsState()
    val context = LocalContext.current

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
                },
                actions = {
                    // Solo habilitamos el botón si tenemos la data completa para guardar
                    if (uiState is RecipeUiState.DetailSuccess) {
                        val receta = (uiState as RecipeUiState.DetailSuccess).recipe

                        FavoriteToggleButton(
                            isFavorite = isFavorite,
                            onToggleClick = {
                                // Serializamos el payload exacto devuelto por la API para uso Offline
                                val recipeJson = Gson().toJson(receta)

                                val entity = FavoriteRecipeEntity(
                                    externalRecipeId = recetaId,
                                    title = receta.title,
                                    imageUrl = receta.image ?: "",
                                    recipeDataJson = recipeJson
                                )
                                // Despachamos el evento UDF hacia el ViewModel
                                favoriteViewModel.onToggleFavorite(entity)
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is RecipeUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is RecipeUiState.Error -> {
                    val errorState = uiState as RecipeUiState.Error
                    val errorDetails = ErrorUtils.getErrorDetails(context, errorState.throwable)
                    com.smart.comida.ui.components.ErrorState(
                        title = errorDetails.title,
                        message = errorState.message.ifBlank { errorDetails.message },
                        onRetry = { recipeViewModel.getRecipeDetail(recetaId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
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

                        // Imprimir ingredientes
                        items(receta.extendedIngredients) { ingrediente ->
                            Text("• ${ingrediente.original}", modifier = Modifier.padding(vertical = 4.dp))
                        }

                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("Pasos", style = MaterialTheme.typography.titleLarge)
                        }

                        // Imprimir pasos
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