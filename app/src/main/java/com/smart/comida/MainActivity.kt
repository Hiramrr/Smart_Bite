package com.smart.comida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.smart.comida.ui.screens.AgregarIngredienteScreen
import com.smart.comida.ui.theme.ComidaTheme // <-- Importación corregida

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // <-- Nombre del tema corregido
            ComidaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AgregarIngredienteScreen(
                        onGuardadoExitoso = {
                            println("¡Ingrediente guardado! Listo para agregar otro o navegar.")
                        }
                    )
                }
            }
        }
    }
}