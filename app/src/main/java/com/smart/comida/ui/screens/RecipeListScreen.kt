package com.smart.comida.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.smart.comida.util.ErrorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.smartbite.data.Recipe
import com.smart.comida.ui.components.EmptyState
import com.smart.comida.ui.components.ShimmerRecipeGrid
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.RecipeUiState
import com.smart.comida.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    onVolver: () -> Unit,
    onRecetaClick: (Int) -> Unit,
    onFavoritasClick: () -> Unit, // <-- Nuevo evento de navegación inyectado
    recipeViewModel: RecipeViewModel = viewModel(),
    onSettingsClick: () -> Unit = {}
) {
    val uiState by recipeViewModel.uiState.collectAsState()
    val isRateLimitActive by recipeViewModel.isRateLimitActive.collectAsState()
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Recetas", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // <-- Nuevo botón de acceso rápido al recetario
                    IconButton(onClick = onFavoritasClick) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Mi Recetario",
                            tint = colorScheme.primary // Resalta el color para invitar a la acción
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        // ... (Tu código de contenido se mantiene exactamente igual)
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                // ... (Sin cambios)
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar receta o ingrediente...", color = colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = colorScheme.outline,
                    focusedBorderColor = colorScheme.primary,
                    unfocusedContainerColor = colorScheme.surface,
                    focusedContainerColor = colorScheme.surface
                ),
                singleLine = true
            )

            Button(
                // ... (Sin cambios)
                onClick = { if (query.isNotBlank()) recipeViewModel.searchRecipes(query) },
                modifier = Modifier.fillMaxWidth(),
                enabled = query.isNotBlank() && !isRateLimitActive && uiState !is RecipeUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Buscar", fontWeight = FontWeight.Bold)
            }

            when (uiState) {
                is RecipeUiState.Loading -> {
                    ShimmerRecipeGrid(count = 6, modifier = Modifier.fillMaxWidth())
                }
                is RecipeUiState.Error -> {
                    val errorState = uiState as RecipeUiState.Error
                    val errorDetails = ErrorUtils.getErrorDetails(context, errorState.throwable)
                    com.smart.comida.ui.components.ErrorState(
                        title = errorDetails.title,
                        message = errorState.message.ifBlank { errorDetails.message },
                        onRetry = { if (query.isNotBlank()) recipeViewModel.searchRecipes(query) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is RecipeUiState.SearchSuccess -> {
                    val recetas = (uiState as RecipeUiState.SearchSuccess).recipes
                    if (recetas.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Restaurant,
                            title = "Sin resultados",
                            description = "Intenta con otro ingrediente o nombre"
                        )
                    } else {
                        LazyVerticalGrid(
                            // ... (Sin cambios)
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(recetas) { receta ->
                                RecipeCard(receta = receta, onClick = { onRecetaClick(receta.id) })
                            }
                        }
                    }
                }
                else -> {
                    EmptyState(
                        icon = Icons.Default.Restaurant,
                        title = "Busca recetas",
                        description = "Encuentra recetas por ingrediente o nombre"
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCard(receta: Recipe, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.outline)
    ) {
        Column {
            AsyncImage(
                model = receta.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                text = receta.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(12.dp),
                maxLines = 2
            )
        }
    }
}
