package com.smart.comida.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smart.comida.data.model.Desperdicio
import com.smart.comida.ui.viewmodel.DespensaViewModel
import com.smart.comida.ui.viewmodel.HistorialDesperdicioUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialDesperdicioScreen(
    viewModel: DespensaViewModel,
    onVolver: () -> Unit
) {
    val uiState = viewModel.historialUiState
    val categoriasPorId = viewModel.categorias.associateBy { it.id }

    LaunchedEffect(Unit) {
        viewModel.cargarHistorialDesperdicio()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Historial de desperdicio") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is HistorialDesperdicioUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is HistorialDesperdicioUiState.Error -> {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is HistorialDesperdicioUiState.Success -> {
                if (uiState.desperdicios.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No hay desperdicios registrados.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.desperdicios) { desperdicio ->
                            val categoria = categoriasPorId[desperdicio.categoriaId]?.nombre ?: "Sin categoría"
                            DesperdicioItem(desperdicio = desperdicio, categoria = categoria)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesperdicioItem(
    desperdicio: Desperdicio,
    categoria: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(desperdicio.nombre, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${desperdicio.cantidad} ${desperdicio.unidad ?: ""}".trim())
                Text(categoria)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Caducidad: ${desperdicio.fechaCaducidad ?: "S/F"}")
            Text("Desechado: ${desperdicio.fechaDesecho.take(10)}")
        }
    }
}
