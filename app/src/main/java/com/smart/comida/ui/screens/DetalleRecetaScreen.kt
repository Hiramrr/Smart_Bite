package com.smart.comida.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.smartbite.data.InstructionStep
import com.google.gson.Gson
import com.smart.comida.data.local.entity.FavoriteRecipeEntity
import com.smart.comida.domain.repository.FavoritesRepository
import com.smart.comida.presentation.components.FavoriteToggleButton
import com.smart.comida.presentation.viewmodel.RecipeDetailViewModel
import com.smart.comida.presentation.viewmodel.RecipeDetailViewModelFactory
import com.smart.comida.presentation.viewmodel.FavoriteActionUiState
import com.smart.comida.ui.viewmodel.AgregarDesdeRecetaState
import com.smart.comida.ui.viewmodel.IngredienteFaltante
import com.smart.comida.ui.viewmodel.ListaComprasViewModel
import com.smart.comida.ui.viewmodel.PrepararRecetaUiState
import com.smart.comida.ui.viewmodel.PrepararRecetaViewModel
import com.smart.comida.ui.viewmodel.RecipeUiState
import com.smart.comida.ui.viewmodel.RecipeViewModel
import com.smart.comida.ui.viewmodel.ResumenIngredienteDescuento
import com.smart.comida.util.ErrorUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRecetaScreen(
    recetaId: Int,
    onVolver: () -> Unit,
    userId: String,
    recipeViewModel: RecipeViewModel = viewModel(),
    listaComprasViewModel: ListaComprasViewModel = viewModel(),
    prepararRecetaViewModel: PrepararRecetaViewModel = viewModel(),
    favoritesRepository: FavoritesRepository,
    onPreparacionExitosa: () -> Unit = {}
) {
    val uiState by recipeViewModel.uiState.collectAsState()
    val prepararEstado = prepararRecetaViewModel.uiState

    val favoriteViewModel: RecipeDetailViewModel = viewModel(
        factory = RecipeDetailViewModelFactory(favoritesRepository, recetaId, userId)
    )

    val isFavorite by favoriteViewModel.isFavorite.collectAsState()
    val favoriteActionState by favoriteViewModel.actionUiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(recetaId) {
        val favoriteRecipe = favoriteViewModel.getFavoriteRecipe()
        recipeViewModel.getRecipeDetail(recetaId, favoriteRecipe?.recipeDataJson)
        prepararRecetaViewModel.resetState()
    }

    LaunchedEffect(favoriteActionState) {
        if (favoriteActionState is FavoriteActionUiState.Message) {
            snackbarHostState.showSnackbar((favoriteActionState as FavoriteActionUiState.Message).text)
            favoriteViewModel.clearActionMessage()
        }
    }

    val agregarEstado by remember { derivedStateOf { listaComprasViewModel.agregarDesdeRecetaState } }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var ingredientesFaltantes by remember { mutableStateOf<List<IngredienteFaltante>>(emptyList()) }

    LaunchedEffect(agregarEstado) {
        when (val estado = agregarEstado) {
            is AgregarDesdeRecetaState.ShowConfirmation -> {
                ingredientesFaltantes = estado.ingredientesFaltantes
                showConfirmationDialog = true
            }
            is AgregarDesdeRecetaState.Success -> {
                val message = estado.message
                listaComprasViewModel.resetearEstadoReceta()
                snackbarHostState.showSnackbar(message)
            }
            is AgregarDesdeRecetaState.AllAvailable -> {
                val message = estado.message
                listaComprasViewModel.resetearEstadoReceta()
                snackbarHostState.showSnackbar(message)
            }
            is AgregarDesdeRecetaState.Error -> {
                val message = estado.message
                listaComprasViewModel.resetearEstadoReceta()
                snackbarHostState.showSnackbar(message)
            }
            else -> {}
        }
    }

    LaunchedEffect(prepararEstado) {
        when (prepararEstado) {
            is PrepararRecetaUiState.Success -> {
                val message = prepararEstado.message
                onPreparacionExitosa()
                prepararRecetaViewModel.resetState()
                snackbarHostState.showSnackbar(message)
            }
            is PrepararRecetaUiState.Error -> {
                val message = prepararEstado.message
                prepararRecetaViewModel.resetState()
                snackbarHostState.showSnackbar(message)
            }
            else -> {}
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmationDialog = false
                listaComprasViewModel.resetearEstadoReceta()
            },
            title = { Text("Ingredientes faltantes") },
            text = {
                Column {
                    Text("Se agregarán los siguientes ingredientes a tu lista de compras:")
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                        items(ingredientesFaltantes) { ing ->
                            Text(
                                text = "• ${ing.nombre} ${formatCantidadUnidad(ing.cantidad, ing.unidad)}".trim(),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog = false
                        listaComprasViewModel.agregarFaltantesALista(ingredientesFaltantes)
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                        listaComprasViewModel.resetearEstadoReceta()
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    when (val estado = prepararEstado) {
        is PrepararRecetaUiState.ReadyToConfirm -> {
            ConfirmarPreparacionDialog(
                descuentos = estado.descuentos,
                onConfirmar = { prepararRecetaViewModel.confirmarPreparacion() },
                onCancelar = { prepararRecetaViewModel.cancelarPreparacion() }
            )
        }
        is PrepararRecetaUiState.Insufficient -> {
            IngredientesInsuficientesDialog(
                faltantes = estado.faltantes,
                onAgregarACompras = {
                    listaComprasViewModel.agregarFaltantesALista(estado.faltantes)
                    prepararRecetaViewModel.resetState()
                },
                onCerrar = { prepararRecetaViewModel.cancelarPreparacion() }
            )
        }
        PrepararRecetaUiState.Updating -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Preparando receta") },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Actualizando tu despensa...")
                    }
                },
                confirmButton = {}
            )
        }
        else -> {}
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Preparación") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (uiState is RecipeUiState.DetailSuccess) {
                        val receta = (uiState as RecipeUiState.DetailSuccess).recipe

                        IconButton(
                            onClick = {
                                listaComprasViewModel.compararIngredientesConReceta(receta.extendedIngredients)
                            }
                        ) {
                            Icon(Icons.Default.AddShoppingCart, "Agregar ingredientes faltantes a lista de compras")
                        }

                        FavoriteToggleButton(
                            isFavorite = isFavorite,
                            onToggleClick = {
                                val recipeJson = Gson().toJson(receta)
                                val entity = FavoriteRecipeEntity(
                                    externalRecipeId = recetaId,
                                    title = receta.title,
                                    imageUrl = receta.image ?: "",
                                    recipeDataJson = recipeJson,
                                    userId = userId
                                )
                                favoriteViewModel.saveFavorite(entity)
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
                    val preparando = prepararEstado is PrepararRecetaUiState.Checking ||
                        prepararEstado is PrepararRecetaUiState.Updating

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AsyncImage(
                                model = receta.image,
                                contentDescription = receta.title,
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(receta.title, style = MaterialTheme.typography.headlineMedium)
                                Text(
                                    "Tiempo: ${receta.readyInMinutes} min • Porciones: ${receta.servings}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    prepararRecetaViewModel.verificarIngredientes(receta.extendedIngredients)
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !preparando
                            ) {
                                if (preparando) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text("Validando despensa...")
                                } else {
                                    Icon(Icons.Default.Restaurant, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Preparar receta", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        item {
                            Text("Ingredientes", style = MaterialTheme.typography.titleLarge)
                        }

                        items(receta.extendedIngredients) { ingrediente ->
                            Text("• ${ingrediente.original}", modifier = Modifier.padding(vertical = 2.dp))
                        }

                        item {
                            Text("Información nutricional", style = MaterialTheme.typography.titleLarge)
                        }

                        item {
                            NutritionSummary(receta = receta)
                        }

                        item {
                            Text("Pasos", style = MaterialTheme.typography.titleLarge)
                        }

                        item {
                            val pasos = receta.analyzedInstructions.firstOrNull()?.steps.orEmpty()
                            PasosRecetaPager(pasos = pasos)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun NutritionSummary(receta: com.example.smartbite.data.RecipeDetail) {
    val nutrients = receta.nutrition?.nutrients.orEmpty()
    if (nutrients.isEmpty()) {
        Text(
            "Información nutricional no disponible para esta receta.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val preferredNames = listOf("Calories", "Protein", "Carbohydrates", "Fat")
    val preferred = preferredNames.mapNotNull { preferredName ->
        nutrients.firstOrNull { it.name.equals(preferredName, ignoreCase = true) }
    }
    val visibleNutrients = (preferred + nutrients).distinctBy { it.name }.take(6)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            visibleNutrients.forEach { nutrient ->
                val label = when (nutrient.name.lowercase(Locale.ROOT)) {
                    "calories" -> "Calorías"
                    "protein" -> "Proteínas"
                    "carbohydrates" -> "Carbohidratos"
                    "fat" -> "Grasas"
                    else -> nutrient.name
                }
                Text("$label: ${String.format(Locale.getDefault(), "%.1f", nutrient.amount)} ${nutrient.unit}")
            }
        }
    }
}

@Composable
private fun ConfirmarPreparacionDialog(
    descuentos: List<ResumenIngredienteDescuento>,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Confirmar preparación") },
        text = {
            Column {
                Text("Se descontarán estos ingredientes de tu despensa:")
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    items(descuentos) { descuento ->
                        Text(
                            text = "• ${descuento.nombre}: ${formatCantidadUnidad(descuento.cantidad, descuento.unidad)}",
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmar) {
                Text("Preparar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun IngredientesInsuficientesDialog(
    faltantes: List<IngredienteFaltante>,
    onAgregarACompras: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Ingredientes insuficientes") },
        text = {
            Column {
                Text("Faltan estos ingredientes para preparar la receta:")
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    items(faltantes) { faltante ->
                        Text(
                            text = "• ${faltante.nombre}: ${formatCantidadUnidad(faltante.cantidad, faltante.unidad)}",
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onAgregarACompras) {
                Text("Agregar a compras")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun PasosRecetaPager(pasos: List<InstructionStep>) {
    if (pasos.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Esta receta no incluye pasos detallados.",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var pasoActual by remember(pasos) { mutableStateOf(0) }
    val paso = pasos[pasoActual]

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Paso ${pasoActual + 1} de ${pasos.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = paso.step,
                style = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { if (pasoActual > 0) pasoActual-- },
                    enabled = pasoActual > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Anterior")
                }
                Button(
                    onClick = { if (pasoActual < pasos.lastIndex) pasoActual++ },
                    enabled = pasoActual < pasos.lastIndex
                ) {
                    Text("Siguiente")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
                }
            }
        }
    }
}

private fun formatCantidadUnidad(cantidad: Double?, unidad: String?): String {
    val cantidadFormateada = cantidad?.let { valor ->
        if (valor % 1.0 == 0.0) {
            valor.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", valor).trimEnd('0').trimEnd('.')
        }
    }.orEmpty()

    return listOf(cantidadFormateada, unidad.orEmpty())
        .filter { it.isNotBlank() }
        .joinToString(" ")
}
