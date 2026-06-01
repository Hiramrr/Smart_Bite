package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.smart.comida.util.ErrorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smart.comida.R
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.RegistrarDesperdicioUiState
import com.smart.comida.ui.viewmodel.RegistrarDesperdicioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarDesperdicioScreen(
    ingredienteId: Int,
    onVolver: () -> Unit,
    onRegistroExitoso: () -> Unit,
    viewModel: RegistrarDesperdicioViewModel = viewModel()
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    val ing = viewModel.ingrediente

    LaunchedEffect(ingredienteId) {
        viewModel.cargarIngrediente(ingredienteId)
    }

    LaunchedEffect(viewModel.uiState) {
        if (viewModel.uiState is RegistrarDesperdicioUiState.Success) {
            onRegistroExitoso()
            viewModel.resetState()
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    if (mostrarConfirmacion && ing != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar desperdicio") },
            text = {
                Text("¿Deseas desechar ${ing.nombre}? Se quitará del inventario activo y quedará registrado en tus estadísticas.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        viewModel.registrarDesperdicio()
                    }
                ) {
                    Text("Desechar", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Registrar desperdicio", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (viewModel.uiState is RegistrarDesperdicioUiState.Error) {
                    val errorState = viewModel.uiState as RegistrarDesperdicioUiState.Error
                    val errorDetails = ErrorUtils.getErrorDetails(context, errorState.throwable)
                    Text(
                        text = errorState.message.ifBlank { errorDetails.message },
                        color = colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = { mostrarConfirmacion = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeExpiring),
                    shape = RoundedCornerShape(16.dp),
                    enabled = viewModel.uiState !is RegistrarDesperdicioUiState.Loading && ing != null
                ) {
                    if (viewModel.uiState is RegistrarDesperdicioUiState.Loading) {
                        CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Confirmar desperdicio", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        val uiState = viewModel.uiState
        if (ing == null && uiState !is RegistrarDesperdicioUiState.Error) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OrangeExpiring)
            }
        } else if (ing == null && uiState is RegistrarDesperdicioUiState.Error) {
            val errorDetails = ErrorUtils.getErrorDetails(context, (uiState as RegistrarDesperdicioUiState.Error).throwable)
            com.smart.comida.ui.components.ErrorState(
                title = errorDetails.title,
                message = (uiState as RegistrarDesperdicioUiState.Error).message.ifBlank { errorDetails.message },
                onRetry = { viewModel.cargarIngrediente(ingredienteId) }
            )
        } else if (ing != null) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!ing.imagenUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ing.imagenUrl,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(60.dp).background(colorScheme.primaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            Text(ing.nombre.take(1), fontWeight = FontWeight.Bold, color = colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(ing.nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colorScheme.onBackground)
                        Text("${ing.cantidad} ${ing.unidad} disponibles", fontSize = 15.sp, color = colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = colorScheme.outline.copy(alpha = 0.5f))

                Text(
                    "Se desechará la cantidad completa: ${ing.cantidad} ${ing.unidad ?: "piezas"}.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.onBackground
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = LightOrange.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(id = R.drawable.ic_desperdicio), contentDescription = null, tint = OrangeExpiring)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Solo se permite desechar un alimento caducado o próximo a caducar. Se registrará en el historial y se quitará del inventario activo.",
                            fontSize = 13.sp,
                            color = OrangeExpiring
                        )
                    }
                }
            }
        }
    }
}
