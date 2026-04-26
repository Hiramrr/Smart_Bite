package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.comida.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarIngredienteScreen(
    onVolver: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = { Text("Agregar ingrediente", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { /* OCR */ }) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = "Escanear ticket")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDark,
                    navigationIconContentColor = TextDark,
                    actionIconContentColor = TextDark
                )
            )
        },
        bottomBar = {
            PaddingValues(16.dp).let { padding ->
                Button(
                    onClick = onGuardadoExitoso,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Guardar ingrediente", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            // Image Placeholder
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(LightGreen, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Mock avocado
                Box(modifier = Modifier.size(80.dp).background(PrimaryGreen, CircleShape))
                
                // Camera icon fab
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .size(40.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Tomar foto", tint = TextDark, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormTextField(label = "Nombre *", placeholder = "Ej. Aguacate")
            
            FormTextField(label = "Categoría *", placeholder = "Seleccionar categoría", readOnly = true)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FormTextField(label = "Cantidad *", placeholder = "Ej. 2", keyboardType = KeyboardType.Number)
                }
                Box(modifier = Modifier.weight(1f)) {
                    FormTextField(label = "Unidad *", placeholder = "Seleccionar", readOnly = true)
                }
            }

            FormTextField(
                label = "Fecha de caducidad *",
                placeholder = "Seleccionar fecha",
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextGray) }
            )

            FormTextField(
                label = "Notas (opcional)",
                placeholder = "Añade notas...",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FormTextField(
    label: String,
    placeholder: String,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = "",
            onValueChange = { },
            readOnly = readOnly,
            placeholder = { Text(placeholder, color = TextGray, fontSize = 14.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            trailingIcon = trailingIcon,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = GrayBorder,
                focusedBorderColor = PrimaryGreen,
                unfocusedContainerColor = CardWhite,
                focusedContainerColor = CardWhite
            )
        )
    }
}
