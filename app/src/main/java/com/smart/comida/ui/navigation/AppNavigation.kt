package com.smart.comida.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.comida.ui.viewmodel.DespensaViewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smart.comida.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val despensaViewModelCompartido: DespensaViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = "despensa"
    ) {
        composable("despensa") {
            DespensaScreen(
                viewModel = despensaViewModelCompartido,
                onAgregarClick = { navController.navigate("agregar") },
                onEditarClick = { idIngrediente ->
                    navController.navigate("editar/$idIngrediente")
                },
                onVerDetalleClick = { idIngrediente ->
                    navController.navigate("detalle_ingrediente/$idIngrediente")
                }
            )
        }

        composable("agregar") {
            AgregarIngredienteScreen(onGuardadoExitoso = { navController.popBackStack() })
        }

        composable(
            route = "editar/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            EditarIngredienteScreen(ingredienteId = id, onGuardadoExitoso = { navController.popBackStack() })
        }

        composable(
            route = "detalle_ingrediente/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetalleIngredienteScreen(
                ingredienteId = id,
                onVolver = { navController.popBackStack() },
                onVerRecetaClick = { idReceta ->
                    navController.navigate("detalle_receta/$idReceta")
                },
                despensaViewModel = despensaViewModelCompartido
            )
        }

        composable(
            route = "detalle_receta/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetalleRecetaScreen(
                recetaId = id,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}