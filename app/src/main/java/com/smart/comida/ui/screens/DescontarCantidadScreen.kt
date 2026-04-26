package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun DescontarCantidadScreen(
    ingredienteId: Int,
    onVolver: () -> Unit,
    onDescontadoExitoso: () -> Unit
) {
    var cantidad by remember { mutableStateOf("") }
    
    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = { Text("Descontar cantidad", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite,
                    titleContentColor = TextDark,
                    navigationIconContentColor = TextDark
                )
            )
        },
        bottomBar = {
            PaddingValues(16.dp).let { padding ->
                Button(
                    onClick = { onDescontadoExitoso() },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Descontar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Ingredient Info Mock
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(Color.LightGray, RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Leche Entera", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
                    Text("1 Litro disponible", fontSize = 14.sp, color = TextGray)
                }
            }

            Text("¿Cuánta cantidad vas a descontar?", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextDark)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = GrayBorder,
                        focusedBorderColor = PrimaryGreen
                    )
                )
                // Mock dropdown
                OutlinedTextField(
                    value = "Litros",
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = GrayBorder,
                        focusedBorderColor = PrimaryGreen
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Motivo (opcional)", fontSize = 14.sp, color = TextGray)
                OutlinedTextField(
                    value = "Seleccionar motivo",
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = GrayBorder,
                        focusedBorderColor = PrimaryGreen
                    )
                )
            }
        }
    }
}
