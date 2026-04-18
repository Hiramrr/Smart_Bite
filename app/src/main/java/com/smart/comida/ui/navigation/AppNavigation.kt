package com.smart.comida.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smart.comida.ui.screens.AgregarIngredienteScreen
import com.smart.comida.ui.screens.DespensaScreen

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
                onAgregarClick = { navController.navigate("agregar") },
                onEditarClick = { idIngrediente ->
                    // Imprime en consola para que veas que funciona.
                    // En el próximo paso, haremos que navegue a la pantalla de edición.
                    println("Se hizo clic para editar el ingrediente con ID: $idIngrediente")
                }
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