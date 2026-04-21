package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.ui.viewmodel.DespensaViewModel
import com.smart.comida.ui.viewmodel.RecipeUiState
import com.smart.comida.ui.viewmodel.RecipeViewModel

val DetailBackground = Color(0xFFE8EFE5)
val WhiteCardBackground = Color.White
val GrayButtonBackground = Color(0xFFF0F0F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleIngredienteScreen(
    ingredienteId: Int,
    onVolver: () -> Unit,
    onEditarClick: (Int) -> Unit,
    onVerRecetaClick: (Int) -> Unit,
    despensaViewModel: DespensaViewModel,
    recipeViewModel: RecipeViewModel = viewModel()
){
    val ingrediente = despensaViewModel.uiState.let { state ->
        if (state is com.smart.comida.ui.viewmodel.DespensaUiState.Success) {
            state.ingredientes.find { it.id == ingredienteId }
        } else null
    }

    val recipeState by recipeViewModel.uiState.collectAsState()
    val categoriaNombre = despensaViewModel.categorias
        .find { it.id == ingrediente?.categoriaId }
        ?.nombre ?: "Sin categoría"
    
    // Estado para el diálogo de confirmación
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(ingrediente) {
        if (ingrediente != null) {
            recipeViewModel.searchRecipes(ingrediente.nombre)
        }
    }

    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar ${ingrediente?.nombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ingrediente?.id != null) {
                            despensaViewModel.eliminarIngrediente(ingrediente.id, ingrediente.imagenUrl)
                            onVolver()
                        }
                        mostrarConfirmacionEliminar = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = DetailBackground,
        contentWindowInsets = WindowInsets(0.dp) // Permite dibujar detrás de las barras del sistema
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(bottom = padding.calculateBottomPadding()) // Solo respetamos el padding inferior de navegación
                .fillMaxSize()
        ) {
            // Top Image Background (ocupa la mitad superior)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f) // Toma el 50% de la altura total
                    .align(Alignment.TopCenter)
            ) {
                if (ingrediente != null && !ingrediente.imagenUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ingrediente.imagenUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(DetailBackground))
                }

                // Sutil gradiente para asegurar la visibilidad de los botones en la parte superior
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp) // Un poco más alto para cubrir el status bar
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // Mueve los iconos debajo de la barra de estado
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.Black)
                    }
                    IconButton(
                        onClick = { /* Add Stock Action */ },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.Black)
                    }
                }
            }

            // Bottom Info Card (ocupa el resto más un solapamiento)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f) // Toma el 55% para crear ese solapamiento sobre la imagen (50% + 5% extra arriba)
                    .align(Alignment.BottomCenter),
                color = WhiteCardBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = ingrediente?.nombre ?: "Detalles",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = categoriaNombre,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                    }

                    item {
                        DetailRow("Categoría", categoriaNombre)
                        DetailRow("Cantidad", "${ingrediente?.cantidad ?: 0} ${ingrediente?.unidad ?: "restantes"}")
                        DetailRow("Agregado", "Recientemente") // Ideally from actual data if we had it
                        DetailRow("Caduca", ingrediente?.fechaCaducidad ?: "S/F")
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        Button(
                            onClick = { ingrediente?.id?.let(onEditarClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LightYellow),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Text("Editar Ingrediente", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { mostrarConfirmacionEliminar = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GrayButtonBackground),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
                        ) {
                            Text("Eliminar", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Sugerencias de recetas
                    item {
                        Text(
                            text = "¿Qué preparar con esto?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    when (recipeState) {
                        is RecipeUiState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = LightYellow)
                                }
                            }
                        }
                        is RecipeUiState.Error -> {
                            item {
                                Text((recipeState as RecipeUiState.Error).message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        is RecipeUiState.SearchSuccess -> {
                            val recetas = (recipeState as RecipeUiState.SearchSuccess).recipes
                            items(recetas) { receta ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onVerRecetaClick(receta.id) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = receta.image,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = receta.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 16.sp)
        Surface(
            color = GrayButtonBackground,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = value,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
