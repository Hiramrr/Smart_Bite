package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
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
import coil.compose.AsyncImage
import com.smart.comida.data.model.Ingrediente
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.DespensaUiState
import com.smart.comida.ui.viewmodel.DespensaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DespensaListScreen(
    viewModel: DespensaViewModel,
    onBackClick: () -> Unit,
    onVerDetalleClick: (Int) -> Unit
) {
    val uiState = viewModel.uiState
    val categorias = viewModel.categorias

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = { Text("Mi Despensa", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Ordenar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDark,
                    navigationIconContentColor = TextDark,
                    actionIconContentColor = TextDark
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = viewModel.filtroSeleccionado == null,
                        onClick = { viewModel.seleccionarFiltroCategoria(null) },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White,
                            containerColor = BackgroundWhite,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = viewModel.filtroSeleccionado == null, borderColor = GrayBorder),
                        shape = CircleShape
                    )
                }
                items(categorias) { categoria ->
                    val isSelected = viewModel.filtroSeleccionado?.id == categoria.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.seleccionarFiltroCategoria(categoria) },
                        label = { Text(categoria.nombre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White,
                            containerColor = BackgroundWhite,
                            labelColor = TextGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = GrayBorder),
                        shape = CircleShape
                    )
                }
            }

            // List of Ingredients
            when (uiState) {
                is DespensaUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is DespensaUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.message, color = RedExpiring)
                    }
                }
                is DespensaUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.ingredientes) { ingrediente ->
                            IngredienteListItem(ingrediente = ingrediente, onClick = { ingrediente.id?.let(onVerDetalleClick) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IngredienteListItem(ingrediente: Ingrediente, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!ingrediente.imagenUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ingrediente.imagenUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.size(48.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(ingrediente.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Text("${ingrediente.cantidad} ${ingrediente.unidad ?: ""}", fontSize = 14.sp, color = TextGray)
        }
        
        // Mocking days left
        Text("2 días", color = OrangeExpiring, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
    Divider(color = BackgroundWhite)
}
