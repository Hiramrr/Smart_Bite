package com.smart.comida.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import com.smart.comida.presentation.viewmodel.RecipeBookUiState
import com.smart.comida.presentation.viewmodel.RecipeBookViewModel
import com.smart.comida.presentation.viewmodel.RecipeBookViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBookScreen(
    favoritesRepository: FavoritesRepository,
    onNavigateToRecipeDetail: (Int) -> Unit, // Para visualizar el detalle (CU-09)
    onNavigateToSearch: () -> Unit // Para el flujo FA-01
) {
    val viewModel: RecipeBookViewModel = viewModel(
        factory = RecipeBookViewModelFactory(favoritesRepository)
    )

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Recetario") },
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
                    ErrorState(message = state.message, modifier = Modifier.align(Alignment.Center))
                }
                is RecipeBookUiState.Success -> {
                    RecipeGrid(
                        recipes = state.recipes,
                        onRecipeClick = onNavigateToRecipeDetail
                    )
                }
            }
        }
    }
}

// =====================================================================
// Componentes Modulares (SRP)
// =====================================================================

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
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Text(
        text = "Error: $message",
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(16.dp)
    )
}

@Composable
private fun RecipeGrid(
    recipes: List<FavoriteRecipeEntity>,
    onRecipeClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(recipes, key = { it.externalRecipeId }) { recipe ->
            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.externalRecipeId) })
        }
    }
}

@Composable
private fun RecipeCard(recipe: FavoriteRecipeEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
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
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}