package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.DespensaUiState
import com.smart.comida.ui.viewmodel.DespensaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DespensaViewModel,
    onVerTodosClick: () -> Unit,
    onVerDetalleClick: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.cargarIngredientes()
    }

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = { Text("Mi Despensa", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { viewModel.cargarIngredientes() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refrescar"
                        )
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDark,
                    actionIconContentColor = TextDark
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Greeting section
            Column {
                Text("¡Hola, Usuario! \uD83D\uDC4B", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("¿Qué vamos a cocinar hoy?", fontSize = 16.sp, color = TextGray)
            }

            // Search Bar
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                placeholder = { Text("Buscar ingrediente...", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GrayBorder,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedContainerColor = CardWhite,
                    focusedContainerColor = CardWhite
                ),
                singleLine = true
            )

            // Category Chips (Horizontal Scroll)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.categorias) { categoria ->
                    val isSelected = viewModel.filtroSeleccionado?.id == categoria.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            if (isSelected) viewModel.seleccionarFiltroCategoria(null)
                            else viewModel.seleccionarFiltroCategoria(categoria)
                        },
                        label = { Text(categoria.nombre, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = GrayBorder
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Resumen de tu despensa
            var isResumenExpanded by remember { mutableStateOf(true) }
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isResumenExpanded = !isResumenExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Resumen de tu despensa", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Icon(
                        imageVector = if (isResumenExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir/Minimizar",
                        tint = TextDark
                    )
                }
                
                AnimatedVisibility(visible = isResumenExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResumenItem(
                            viewModel.resumen.total.toString(),
                            "Ingredientes",
                            LightGreen,
                            PrimaryGreen,
                            Icons.Default.Kitchen
                        )
                        ResumenItem(
                            viewModel.resumen.porVencer.toString(),
                            "Por vencer",
                            LightOrange,
                            OrangeExpiring,
                            Icons.Default.Warning
                        )
                        ResumenItem(
                            viewModel.resumen.bajosStock.toString(),
                            "Bajos en stock",
                            LightPurple,
                            PurpleAccent,
                            Icons.Default.TrendingDown
                        )
                    }
                }
            }

            // Buen momento para comprar
            Card(
                colors = CardDefaults.cardColors(containerColor = LightGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Buen momento para comprar \uD83D\uDED2", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        Text(
                            "Tienes ${viewModel.resumen.porVencer} ingredientes por vencer en los próximos 7 días.",
                            fontSize = 12.sp,
                            color = PrimaryGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onVerTodosClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PrimaryGreen),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Ver lista", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Image placeholder
                    Box(modifier = Modifier.size(80.dp).background(Color.Transparent)) {
                        // In reality an image goes here
                    }
                }
            }

            // Por vencer pronto
            if (uiState is DespensaUiState.Success) {
                val ingredientes = uiState.ingredientes.take(4) // Mock taking first 4
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Por vencer pronto", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text(
                            "Ver todos",
                            fontSize = 14.sp,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onVerTodosClick() }
                        )
                    }
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ingredientes) { ingrediente ->
                            IngredienteVencerCard(ingrediente) { ingrediente.id?.let(onVerDetalleClick) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumenItem(value: String, label: String, bgColor: Color, iconColor: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
        Text(label, fontSize = 10.sp, color = TextGray, textAlign = TextAlign.Center)
    }
}

@Composable
fun IngredienteVencerCard(ingrediente: Ingrediente, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(100.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrayBorder)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = LightRed,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("2 días", color = RedExpiring, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
            }
            if (!ingrediente.imagenUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ingrediente.imagenUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(48.dp).background(Color.LightGray, CircleShape))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(ingrediente.nombre, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1, textAlign = TextAlign.Center)
            Text("${ingrediente.cantidad} ${ingrediente.unidad ?: ""}", fontSize = 10.sp, color = TextGray, maxLines = 1)
        }
    }
}
