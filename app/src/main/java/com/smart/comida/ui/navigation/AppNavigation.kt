package com.smart.comida.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smart.comida.ui.screens.AgregarIngredienteScreen
import com.smart.comida.ui.screens.DespensaScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.smart.comida.ui.screens.EditarIngredienteScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "despensa"
    ) {
        composable("despensa") {
            DespensaScreen(
                onAgregarClick = { navController.navigate("agregar") },
                onEditarClick = { idIngrediente ->
                    // ¡Navegamos a la nueva pantalla pasándole el ID!
                    navController.navigate("editar/$idIngrediente")
                }
            )
        }

        composable("agregar") {
            AgregarIngredienteScreen(
                onGuardadoExitoso = { navController.popBackStack() }
            )
        }

        // Ruta 3: Formulario de Editar (Requiere un argumento 'id')
        composable(
            route = "editar/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            // Extraemos el ID de la ruta
            val id = backStackEntry.arguments?.getInt("id") ?: 0

            EditarIngredienteScreen(
                ingredienteId = id,
                onGuardadoExitoso = { navController.popBackStack() }
            )
        }
    }
}