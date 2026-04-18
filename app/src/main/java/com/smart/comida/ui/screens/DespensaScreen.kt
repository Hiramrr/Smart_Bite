package com.smart.comida.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.data.model.Ingrediente // <-- Asegúrate de que esta importación apunte a tu nueva carpeta 'model'
import com.smart.comida.ui.viewmodel.DespensaUiState
import com.smart.comida.ui.viewmodel.DespensaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DespensaScreen(
    viewModel: DespensaViewModel = viewModel(),
    onAgregarClick: () -> Unit,
    onEditarClick: (Int) -> Unit // Recibirá el ID del ingrediente a editar
) {
    val uiState = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.cargarIngredientes()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mi Despensa") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregarClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (uiState) {
                is DespensaUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DespensaUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DespensaUiState.Success -> {
                    if (uiState.ingredientes.isEmpty()) {
                        Text("No hay ingredientes en tu despensa.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        // LA MAGIA DE LA CUADRÍCULA: 2 columnas
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.ingredientes) { ingrediente ->
                                IngredienteCard(
                                    ingrediente = ingrediente,
                                    onEditarClick = {
                                        if (ingrediente.id != null) onEditarClick(ingrediente.id)
                                    },
                                    onEliminarClick = {
                                        if (ingrediente.id != null) viewModel.eliminarIngrediente(ingrediente.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// EL DISEÑO DE TU TARJETA
@Composable
fun IngredienteCard(
    ingrediente: Ingrediente,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f), // Proporción ligeramente más alta que ancha, como tu imagen
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Separa iconos, texto y píldora
        ) {
            // Iconos superiores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onEliminarClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
                IconButton(onClick = onEditarClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
            }

            // Textos centrales
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = ingrediente.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${ingrediente.cantidad} ${ingrediente.unidad ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Píldora inferior de Caducidad
            Surface(
                shape = RoundedCornerShape(50), // Bordes súper redondeados
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Caduca: ${ingrediente.fechaCaducidad ?: "S/F"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                )
            }
        }
    }
}