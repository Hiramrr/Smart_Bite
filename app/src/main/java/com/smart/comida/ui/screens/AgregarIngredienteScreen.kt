package com.smart.comida.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.util.ErrorUtils
import coil.compose.AsyncImage
import com.smart.comida.ui.components.BarcodeScannerView
import com.smart.comida.ui.theme.*
import com.smart.comida.ui.viewmodel.IngredienteUiState
import com.smart.comida.ui.viewmodel.IngredienteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteScreen(
    onVolver: () -> Unit,
    onGuardadoExitoso: () -> Unit,
    viewModel: IngredienteViewModel = viewModel(),
    prefilledNombre: String? = null,
    prefilledCantidad: String? = null,
    prefilledUnidad: String? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val colorScheme = MaterialTheme.colorScheme

    // Estados del formulario
    var nombre by remember { mutableStateOf(prefilledNombre ?: "") }
    var categoriaSeleccionada by remember { mutableStateOf<com.smart.comida.data.model.Categoria?>(null) }
    var cantidad by remember { mutableStateOf(prefilledCantidad ?: "") }
    var unidad by remember { mutableStateOf(prefilledUnidad ?: "") }
    var fechaCaducidad by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrlFromApi by remember { mutableStateOf<String?>(null) }

    // Estado para el escáner
    var showScanner by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) showScanner = true
    }

    // Actualizar campos cuando se detecta un producto
    LaunchedEffect(viewModel.productoEscaneado) {
        viewModel.productoEscaneado?.let { product ->
            nombre = product.nombre
            imageUrlFromApi = product.imagenUrl
            viewModel.clearScannedProduct()
            showScanner = false
        }
    }

    // Estados para menús y diálogos
    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedUnidad by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val unidades = listOf("Kg", "Gramos", "Litros", "Piezas")

    // Cargar categorías al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarCategorias()
    }

    // Manejar estados del ViewModel
    LaunchedEffect(viewModel.uiState) {
        if (viewModel.uiState is IngredienteUiState.Success) {
            onGuardadoExitoso()
            viewModel.resetState()
        }
    }

    // Launcher para imagen
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    // DatePicker State
    val datePickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        fechaCaducidad = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showScanner && hasCameraPermission) {
        BarcodeScannerView(
            onBarcodeDetected = { barcode ->
                viewModel.buscarProductoPorBarcode(barcode)
            },
            onClose = { showScanner = false }
        )
        return // No mostramos el resto de la pantalla mientras el escáner está activo
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Agregar ingrediente", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (hasCameraPermission) {
                            showScanner = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear producto")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground,
                    navigationIconContentColor = colorScheme.onBackground,
                    actionIconContentColor = colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                if (viewModel.uiState is IngredienteUiState.Error) {
                    val errorState = viewModel.uiState as IngredienteUiState.Error
                    val errorDetails = ErrorUtils.getErrorDetails(context, errorState.throwable)
                    Text(
                        text = errorState.message.ifBlank { errorDetails.message },
                        color = colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = {
                        val imageBytes = imageUri?.let { uriToByteArray(context, it) }
                        viewModel.guardarIngrediente(
                            nombre = nombre,
                            cantidadStr = cantidad,
                            unidad = unidad,
                            fechaCaducidad = fechaCaducidad,
                            categoriaId = categoriaSeleccionada?.id,
                            imagenBytes = imageBytes,
                            imageUrlFromApi = imageUrlFromApi
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = viewModel.uiState !is IngredienteUiState.Loading
                ) {
                    if (viewModel.uiState is IngredienteUiState.Loading) {
                        CircularProgressIndicator(color = colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Guardar ingrediente", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image Picker
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(LightGreen)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!imageUrlFromApi.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrlFromApi,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, null, tint = PrimaryGreen, modifier = Modifier.size(80.dp))
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(40.dp)
                        .background(colorScheme.surface, CircleShape)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = colorScheme.onSurface, modifier = Modifier.size(20.dp))
                }
            }

            FormTextField(
                label = "Nombre *",
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "Ej. Aguacate"
            )

            // Selector de Categoría
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Categoría *", color = colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                ExposedDropdownMenuBox(
                    expanded = expandedCategoria,
                    onExpandedChange = { expandedCategoria = it }
                ) {
                    OutlinedTextField(
                        value = categoriaSeleccionada?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Seleccionar categoría", color = colorScheme.onSurfaceVariant) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = colorScheme.outline,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedContainerColor = colorScheme.surface,
                            focusedContainerColor = colorScheme.surface
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }
                    ) {
                        viewModel.categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = {
                                    categoriaSeleccionada = cat
                                    expandedCategoria = false
                                }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FormTextField(
                        label = "Cantidad *",
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        placeholder = "Ej. 2",
                        keyboardType = KeyboardType.Number
                    )
                }
                // Selector de Unidad
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Unidad *", color = colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    ExposedDropdownMenuBox(
                        expanded = expandedUnidad,
                        onExpandedChange = { expandedUnidad = it }
                    ) {
                        OutlinedTextField(
                            value = unidad,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Unidad", color = colorScheme.onSurfaceVariant) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnidad) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = colorScheme.outline,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedContainerColor = colorScheme.surface
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUnidad,
                            onDismissRequest = { expandedUnidad = false }
                        ) {
                            unidades.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unidad = u
                                        expandedUnidad = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Selector de Fecha
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fecha de caducidad *", color = colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = fechaCaducidad,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Seleccionar fecha", color = colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, null, tint = colorScheme.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colorScheme.outline,
                        focusedBorderColor = colorScheme.primary,
                        unfocusedContainerColor = colorScheme.surface,
                        focusedContainerColor = colorScheme.surface
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Función auxiliar para convertir Uri a ByteArray
fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            placeholder = { Text(placeholder, color = colorScheme.onSurfaceVariant, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = colorScheme.outline,
                focusedBorderColor = colorScheme.primary,
                unfocusedContainerColor = colorScheme.surface,
                focusedContainerColor = colorScheme.surface
            )
        )
    }
}
