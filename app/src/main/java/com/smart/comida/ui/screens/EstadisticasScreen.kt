package com.smart.comida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.comida.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen() {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = BackgroundWhite,
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendario")
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
            // Month Dropdown Mock
            OutlinedTextField(
                value = "Este mes",
                onValueChange = { },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = GrayBorder,
                    focusedBorderColor = PrimaryGreen,
                    unfocusedContainerColor = CardWhite,
                    focusedContainerColor = CardWhite
                )
            )

            // Ahorro estimado chart Mock
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ahorro estimado", color = TextGray, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$320", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = LightGreen, shape = RoundedCornerShape(4.dp)) {
                            Text("↑ 18%", color = PrimaryGreen, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    // Mock Chart
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(LightGreen, RoundedCornerShape(8.dp)))
                }
            }

            // Ingredientes por vencer Mock
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Ingredientes por vencer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Próximos 7 días", fontSize = 12.sp, color = TextGray)
                    }
                    Text("6", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }

                // Horizontal list mock of small circular images
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(5) {
                        Box(modifier = Modifier.size(40.dp).background(Color.LightGray, CircleShape))
                    }
                    item {
                        Box(
                            modifier = Modifier.size(40.dp).background(LightGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+1", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
