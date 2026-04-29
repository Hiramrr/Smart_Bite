package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
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
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.DespensaUiState
import com.smart.comida.ui.viewmodel.DespensaViewModel
import com.smart.comida.ui.viewmodel.RecipeUiState
import com.smart.comida.ui.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleIngredienteScreen(
    ingredienteId: Int,
    onVolver: () -> Unit,
    onEditarClick: (Int) -> Unit,
    onDescontarClick: (Int) -> Unit,
    onVerRecetaClick: (Int) -> Unit,
    despensaViewModel: DespensaViewModel,
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val uiState = despensaViewModel.uiState
    val scrollState = rememberScrollState()
    
    val ingrediente = if (uiState is DespensaUiState.Success) {
        uiState.ingredientes.find { it.id == ingredienteId }
    } else null

    val recipeState by recipeViewModel.uiState.collectAsState()

    LaunchedEffect(ingrediente) {
        if (ingrediente != null) {
            recipeViewModel.searchRecipes(ingrediente.nombre)
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { ingrediente?.id?.let { onEditarClick(it) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    navigationIconContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        if (ingrediente == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Ingrediente no encontrado")
            }
            return@Scaffold
        }
        
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Big image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(LightOrange, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!ingrediente.imagenUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ingrediente.imagenUrl,
                        contentDescription = ingrediente.nombre,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(100.dp).background(colorScheme.surface, CircleShape))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(ingrediente.nombre, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorScheme.onBackground)

                val categoriaNombre = despensaViewModel.categorias.find { it.id == ingrediente.categoriaId }?.nombre ?: "Sin categoría"
                Surface(color = LightBlue, shape = RoundedCornerShape(16.dp)) {
                    Text(categoriaNombre, color = BlueText, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cantidad", color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${ingrediente.cantidad} ${ingrediente.unidad ?: ""}", color = colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vence el", color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    val fechaFormateada = if (!ingrediente.fechaCaducidad.isNullOrEmpty()) {
                        try {
                            val fecha = java.time.LocalDate.parse(ingrediente.fechaCaducidad)
                            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            fecha.format(formatter)
                        } catch (e: Exception) {
                            ingrediente.fechaCaducidad
                        }
                    } else "Sin fecha"

                    Text(fechaFormateada, color = colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    
                    // Cálculo de días restantes
                    if (!ingrediente.fechaCaducidad.isNullOrEmpty()) {
                        val caducidadData = try {
                            val hoy = java.time.LocalDate.now()
                            val fechaCad = java.time.LocalDate.parse(ingrediente.fechaCaducidad)
                            val diff = java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaCad)
                            
                            when {
                                diff < 0 -> "Caducado" to colorScheme.error
                                diff == 0L -> "Caduca hoy" to RedExpiring
                                diff == 1L -> "Caduca mañana" to OrangeExpiring
                                else -> "($diff días)" to OrangeExpiring
                            }
                        } catch (e: Exception) {
                            null
                        }

                        caducidadData?.let { (texto, color) ->
                            Text(text = texto, color = color, fontSize = 12.sp)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Comprado el", color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("10/05/2024", color = colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ubicación", color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Refrigerador", color = colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            Column {
                Text("Notas", color = colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Sin notas", color = colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(icon = Icons.Default.Edit, label = "Editar", iconColor = PrimaryGreen, bgColor = LightGreen) {
                    ingrediente.id?.let { onEditarClick(it) }
                }
                ActionButton(icon = Icons.Default.Remove, label = "Descontar", iconColor = PurpleAccent, bgColor = LightPurple) {
                    ingrediente.id?.let { onDescontarClick(it) }
                }
                ActionButton(icon = Icons.Default.Delete, label = "Eliminar", iconColor = RedExpiring, bgColor = LightRed) {
                    // TODO logic
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Recipes Section
            Text(
                text = "¿Qué preparar con esto?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            when (recipeState) {
                is RecipeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorScheme.primary)
                    }
                }
                is RecipeUiState.Error -> {
                    Text((recipeState as RecipeUiState.Error).message, color = colorScheme.error)
                }
                is RecipeUiState.SearchSuccess -> {
                    val recetas = (recipeState as RecipeUiState.SearchSuccess).recipes
                    if (recetas.isEmpty()) {
                        Text("No se encontraron recetas", color = colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            recetas.forEach { receta ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colorScheme.surface, RoundedCornerShape(12.dp))
                                        .clickable { onVerRecetaClick(receta.id) }
                                        .padding(12.dp),
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
                                    Text(text = receta.title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, iconColor: Color, bgColor: Color, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier.size(56.dp).background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = colorScheme.onBackground, fontWeight = FontWeight.Medium)
    }
}
