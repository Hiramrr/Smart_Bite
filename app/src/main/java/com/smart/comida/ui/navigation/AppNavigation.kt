package com.smart.comida.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smart.comida.data.local.AppDatabase
import com.smart.comida.data.repository.FavoritesRepositoryImpl
import com.smart.comida.ui.screens.*
import com.smart.comida.ui.theme.PrimaryGreen
import com.smart.comida.ui.viewmodel.AuthViewModel
import com.smart.comida.ui.viewmodel.DespensaViewModel
import com.smart.comida.ui.viewmodel.ThemeViewModel

@Composable
fun AppNavigation(themeViewModel: ThemeViewModel = viewModel(), authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val despensaViewModelCompartido: DespensaViewModel = viewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val favoritesRepository = remember { FavoritesRepositoryImpl(database.favoriteRecipeDao()) }

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn == false && currentRoute != "login") {
            navController.navigate("login") {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
        if (isUserLoggedIn == true && currentRoute == "login") {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    if (isUserLoggedIn == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val colorScheme = MaterialTheme.colorScheme

    Scaffold(
        bottomBar = {
            val showBottomBar = currentRoute in listOf("dashboard", "despensa_list", "lista_compras", "estadisticas", "recetas", "profile")
            if (showBottomBar) {
                NavigationBar(
                    containerColor = colorScheme.surface,
                    contentColor = colorScheme.onSurfaceVariant,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Despensa") },
                        label = { Text("Despensa") },
                        selected = currentRoute == "dashboard" || currentRoute == "despensa_list",
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorScheme.primary,
                            selectedTextColor = colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            unselectedTextColor = colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Compras") },
                        label = { Text("Compras") },
                        selected = currentRoute == "lista_compras",
                        onClick = {
                            navController.navigate("lista_compras") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorScheme.primary,
                            selectedTextColor = colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            unselectedTextColor = colorScheme.onSurfaceVariant
                        )
                    )

                    // The floating + button in the middle
                    Box(modifier = Modifier.padding(top = 12.dp)) {
                        FloatingActionButton(
                            onClick = { navController.navigate("agregar") },
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp),
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar")
                        }
                    }

                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Recetas") },
                        label = { Text("Recetas") },
                        selected = currentRoute == "recetas",
                        onClick = {
                            navController.navigate("recetas") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorScheme.primary,
                            selectedTextColor = colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            unselectedTextColor = colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") },
                        selected = currentRoute == "profile",
                        onClick = {
                            navController.navigate("profile") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colorScheme.primary,
                            selectedTextColor = colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            unselectedTextColor = colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isUserLoggedIn == true) "dashboard" else "login",
            modifier = Modifier
                .padding(if (currentRoute == "login") androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(
                "login",
                enterTransition = { fadeEnterTransition(this) },
                exitTransition = { fadeExitTransition(this) },
                popEnterTransition = { fadeEnterTransition(this) },
                popExitTransition = { fadeExitTransition(this) }
            ) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                "dashboard",
                enterTransition = { fadeEnterTransition(this) },
                exitTransition = { fadeExitTransition(this) },
                popEnterTransition = { fadeEnterTransition(this) },
                popExitTransition = { fadeExitTransition(this) }
            ) {
                val userName by authViewModel.userName.collectAsState()
                DashboardScreen(
                    viewModel = despensaViewModelCompartido,
                    userName = userName ?: "Usuario",
                    onVerTodosClick = { navController.navigate("despensa_list") },
                    onVerDetalleClick = { id -> navController.navigate("detalle_ingrediente/$id") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            composable(
                "despensa_list",
                enterTransition = { fadeEnterTransition(this) },
                exitTransition = { fadeExitTransition(this) },
                popEnterTransition = { fadeEnterTransition(this) },
                popExitTransition = { fadeExitTransition(this) }
            ) {
                DespensaListScreen(
                    viewModel = despensaViewModelCompartido,
                    onBackClick = { navController.popBackStack() },
                    onVerDetalleClick = { id -> navController.navigate("detalle_ingrediente/$id") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            composable(
                "agregar",
                enterTransition = { slideEnterTransition(this) },
                exitTransition = { slideExitTransition(this) },
                popEnterTransition = { slidePopEnterTransition(this) },
                popExitTransition = { slidePopExitTransition(this) }
            ) {
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
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
                enterTransition = { slideEnterTransition(this) },
                exitTransition = { slideExitTransition(this) },
                popEnterTransition = { slidePopEnterTransition(this) },
                popExitTransition = { slidePopExitTransition(this) }
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
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
                enterTransition = { slideEnterTransition(this) },
                exitTransition = { slideExitTransition(this) },
                popEnterTransition = { slidePopEnterTransition(this) },
                popExitTransition = { slidePopExitTransition(this) }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                DetalleIngredienteScreen(
                    ingredienteId = id,
                    onVolver = { navController.popBackStack() },
                    onEditarClick = { idIngrediente ->
                        navController.navigate("editar/$idIngrediente")
                    },
                    onDescontarClick = { idIngrediente ->
                        navController.navigate("descontar/$idIngrediente")
                    },
                    onRegistrarDesperdicioClick = { idIngrediente ->
                        navController.navigate("registrar_desperdicio/$idIngrediente")
                    },
                    onVerRecetaClick = { idReceta ->
                        navController.navigate("detalle_receta/$idReceta")
                    },
                    despensaViewModel = despensaViewModelCompartido
                )
            }

            composable(
                route = "detalle_receta/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
                enterTransition = { slideEnterTransition(this) },
                exitTransition = { slideExitTransition(this) },
                popEnterTransition = { slidePopEnterTransition(this) },
                popExitTransition = { slidePopExitTransition(this) }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0

                DetalleRecetaScreen(
                    recetaId = id,
                    onVolver = { navController.popBackStack() },
                    favoritesRepository = favoritesRepository
                )
            }

            composable(
                route = "descontar/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
                enterTransition = { slideEnterTransition(this) },
                exitTransition = { slideExitTransition(this) },
                popEnterTransition = { slidePopEnterTransition(this) },
                popExitTransition = { slidePopExitTransition(this) }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                DescontarCantidadScreen(
                    ingredienteId = id,
                    onVolver = { navController.popBackStack() },
                    onDescontadoExitoso = {
                        despensaViewModelCompartido.cargarIngredientes()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "registrar_desperdicio/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
                enterTransition = { slideEnterTransition(this) },
                exitTransition = { slideExitTransition(this) },
                popEnterTransition = { slidePopEnterTransition(this) },
                popExitTransition = { slidePopExitTransition(this) }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                RegistrarDesperdicioScreen(
                    ingredienteId = id,
                    onVolver = { navController.popBackStack() },
                    onRegistroExitoso = {
                        despensaViewModelCompartido.cargarIngredientes()
                        navController.popBackStack()
                    }
                )
            }

            composable(
                "lista_compras",
                enterTransition = { fadeEnterTransition(this) },
                exitTransition = { fadeExitTransition(this) },
                popEnterTransition = { fadeEnterTransition(this) },
                popExitTransition = { fadeExitTransition(this) }
            ) {
                ListaComprasScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            composable(
                "estadisticas",
                enterTransition = { fadeEnterTransition(this) },
                exitTransition = { fadeExitTransition(this) },
                popEnterTransition = { fadeEnterTransition(this) },
                popExitTransition = { fadeExitTransition(this) }
            ) {
                EstadisticasScreen(
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            composable(
                "settings",
                enterTransition = { slideEnterTransition(this) },
                exitTransition = { slideExitTransition(this) },
                popEnterTransition = { slidePopEnterTransition(this) },
                popExitTransition = { slidePopExitTransition(this) }
            ) {
                SettingsScreen(
                    onVolver = { navController.popBackStack() },
                    themeViewModel = themeViewModel,
                    onSignOut = { authViewModel.signOut() }
                )
            }

            composable(
                "recetas",
                enterTransition = { fadeEnterTransition(this) },
                exitTransition = { fadeExitTransition(this) },
                popEnterTransition = { fadeEnterTransition(this) },
                popExitTransition = { fadeExitTransition(this) }
            ) {
                RecipeListScreen(
                    onVolver = { navController.popBackStack() },
                    onRecetaClick = { id -> navController.navigate("detalle_receta/$id") },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }

            composable(
                "profile",
                enterTransition = { fadeEnterTransition(this) },
                exitTransition = { fadeExitTransition(this) },
                popEnterTransition = { fadeEnterTransition(this) },
                popExitTransition = { fadeExitTransition(this) }
            ) {
                val userName by authViewModel.userName.collectAsState()
                val userEmail by authViewModel.userEmail.collectAsState()
                val userAvatarUrl by authViewModel.userAvatarUrl.collectAsState()
                ProfileScreen(
                    userName = userName ?: "Usuario",
                    userEmail = userEmail,
                    userAvatarUrl = userAvatarUrl,
                    onSettingsClick = { navController.navigate("settings") },
                    onSignOut = { authViewModel.signOut() }
                )
            }
        }
    }
}
