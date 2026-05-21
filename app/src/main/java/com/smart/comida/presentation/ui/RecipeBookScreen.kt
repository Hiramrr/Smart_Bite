package com.smart.comida.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import com.smart.comida.presentation.viewmodel.RecipeBookUiState
import com.smart.comida.presentation.viewmodel.RecipeBookViewModel
import com.smart.comida.presentation.viewmodel.RecipeBookViewModelFactory
import com.smart.comida.util.ErrorUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBookScreen(
    favoritesRepository: FavoritesRepository,
    userId: String,
    onNavigateToRecipeDetail: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: RecipeBookViewModel = viewModel(
        factory = RecipeBookViewModelFactory(favoritesRepository, userId)
    )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Estado local para manejar el flujo alternativo FA-01 del Diálogo de Confirmación
    var recipeToDelete by remember { mutableStateOf<FavoriteRecipeEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Recetario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar a la pantalla anterior"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is RecipeBookUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is RecipeBookUiState.Empty -> {
                    EmptyRecipeBook(onNavigateToSearch, modifier = Modifier.align(Alignment.Center))
                }
                is RecipeBookUiState.Error -> {
                    val errorDetails = ErrorUtils.getErrorDetails(context, state.throwable)
                    com.smart.comida.ui.components.ErrorState(
                        title = errorDetails.title,
                        message = state.message.ifBlank { errorDetails.message },
                        onRetry = { viewModel.loadFavorites() }, // Recarga reactiva
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is RecipeBookUiState.Success -> {
                    RecipeGrid(
                        recipes = state.recipes,
                        onRecipeClick = onNavigateToRecipeDetail,
                        onDeleteClick = { recipe -> recipeToDelete = recipe }
                    )
                }
            }

            // Inyección declarativa del Diálogo de Confirmación (Pasos 3, 4, FA-01)
            recipeToDelete?.let { recipe ->
                DeleteConfirmationDialog(
                    recipeTitle = recipe.title,
                    onConfirm = {
                        viewModel.deleteRecipeFromFavorites(recipe.externalRecipeId)
                        recipeToDelete = null // Cierra el diálogo después de confirmar
                    },
                    onDismiss = {
                        recipeToDelete = null // FA-01: Cancelar eliminación sin alterar el estado remoto
                    }
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    recipeTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Eliminar de favoritos") },
        text = { Text(text = "¿Estás seguro de que deseas quitar \"$recipeTitle\" de tu recetario personal?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Confirmar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}

@Composable
private fun EmptyRecipeBook(onNavigateToSearch: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu recetario está vacío.",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Explora y guarda tus recetas favoritas para verlas aquí incluso sin conexión.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onNavigateToSearch) {
            Text("Buscar recetas")
        }
    }
}

@Composable
private fun RecipeGrid(
    recipes: List<FavoriteRecipeEntity>,
    onRecipeClick: (Int) -> Unit,
    onDeleteClick: (FavoriteRecipeEntity) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(recipes, key = { it.externalRecipeId }) { recipe ->
            RecipeCard(
                recipe = recipe,
                onClick = { onRecipeClick(recipe.externalRecipeId) },
                onDeleteClick = { onDeleteClick(recipe) }
            )
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: FavoriteRecipeEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 44.dp)
                )
            }
            // Icono estructurado según CU-12 Paso 2
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar de favoritos",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}