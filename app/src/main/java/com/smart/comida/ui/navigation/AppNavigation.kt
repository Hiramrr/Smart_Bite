package com.smart.comida.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smart.comida.ui.screens.*
import com.smart.comida.ui.viewmodel.DespensaViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val despensaViewModelCompartido: DespensaViewModel = viewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute == "despensa" || currentRoute == "lista_compras") {
                NavigationBar(
                    containerColor = Color(0xFF2C2C2E), // DarkCardBackground color from your theme
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Kitchen, contentDescription = "Despensa") },
                        label = { Text("Despensa") },
                        selected = currentRoute == "despensa",
                        onClick = {
                            navController.navigate("despensa") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFFEBEB9B), // LightYellow
                            indicatorColor = Color(0xFFEBEB9B),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Lista") },
                        label = { Text("Lista") },
                        selected = currentRoute == "lista_compras",
                        onClick = {
                            navController.navigate("lista_compras") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color(0xFFEBEB9B),
                            indicatorColor = Color(0xFFEBEB9B),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "despensa",
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable("despensa") {
                DespensaScreen(
                    viewModel = despensaViewModelCompartido,
                    onAgregarClick = { navController.navigate("agregar") },
                    onHistorialDesperdicioClick = { navController.navigate("historial_desperdicio") },
                    onEditarClick = { idIngrediente ->
                        navController.navigate("editar/$idIngrediente")
                    },
                    onVerDetalleClick = { idIngrediente ->
                        navController.navigate("detalle_ingrediente/$idIngrediente")
                    }
                )
            }

            composable("agregar") {
                AgregarIngredienteScreen(
                    onVolver = { navController.popBackStack() },
                    onGuardadoExitoso = {
                        despensaViewModelCompartido.cargarIngredientes()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "editar/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                EditarIngredienteScreen(
                    ingredienteId = id,
                    onVolver = { navController.popBackStack() },
                    onGuardadoExitoso = {
                        despensaViewModelCompartido.cargarIngredientes()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "detalle_ingrediente/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                DetalleIngredienteScreen(
                    ingredienteId = id,
                    onVolver = { navController.popBackStack() },
                    onEditarClick = { idIngrediente ->
                        navController.navigate("editar/$idIngrediente")
                    },
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

            composable("historial_desperdicio") {
                HistorialDesperdicioScreen(
                    viewModel = despensaViewModelCompartido,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable("lista_compras") {
                ListaComprasScreen()
            }
        }
    }
}
