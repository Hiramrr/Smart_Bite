package com.smart.comida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smart.comida.ui.screens.AgregarIngredienteScreen
import com.smart.comida.ui.screens.DespensaScreen
import com.smart.comida.ui.theme.ComidaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComidaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "despensa" // Pantalla de inicio
    ) {
        // Ruta 1: Lista de Despensa
        composable("despensa") {
            DespensaScreen(
                onAgregarClick = { navController.navigate("agregar") }
            )
        }

        // Ruta 2: Formulario de Agregar
        composable("agregar") {
            AgregarIngredienteScreen(
                onGuardadoExitoso = {
                    // Al guardar, regresamos a la pantalla anterior
                    navController.popBackStack()
                }
            )
        }
    }
}