package com.lobsterclawe.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.lobsterclawe.App
import com.lobsterclawe.ui.chat.*
import com.lobsterclawe.ui.grocery.*
import com.lobsterclawe.ui.home.*
import com.lobsterclawe.ui.onboarding.*
import com.lobsterclawe.ui.recipe.*
import com.lobsterclawe.ui.saved.*
import com.lobsterclawe.ui.settings.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Onboarding : Screen("onboarding", "Onboarding")
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Grocery : Screen("grocery", "Grocery", Icons.Default.ShoppingCart)
    data object Saved : Screen("saved", "Saved", Icons.Default.Bookmark)
    data object Chat : Screen("chat", "Chat", Icons.AutoMirrored.Filled.Chat)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object RecipeDetail : Screen("recipe/{id}", "Recipe")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val app = context.applicationContext as App

    val items = listOf(Screen.Home, Screen.Grocery, Screen.Saved, Screen.Chat, Screen.Settings)

    val homeViewModel: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(app.prefs, app.openRouterClient) as T
        }
    })

    val savedViewModel: SavedViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SavedViewModel(app.database.savedRecipeDao()) as T
        }
    })

    val groceryViewModel: GroceryViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return GroceryViewModel(app.openClawClient) as T
        }
    })

    val chatViewModel: ChatViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(app.openRouterClient) as T
        }
    })

    val settingsViewModel: SettingsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(app.prefs, app.openClawClient) as T
        }
    })

    val startDestination = if (app.prefs.onboardingDone) Screen.Home.route else Screen.Onboarding.route

    Scaffold(
        bottomBar = {
            val showBottomNav = currentDestination?.route in items.map { it.route }
            if (showBottomNav) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = com.lobsterclawe.ui.theme.Teal,
                                selectedTextColor = com.lobsterclawe.ui.theme.Teal,
                                indicatorColor = com.lobsterclawe.ui.theme.TealLight
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = startDestination, Modifier.padding(innerPadding)) {
            composable(Screen.Onboarding.route) {
                val onboardingViewModel: OnboardingViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return OnboardingViewModel(app.prefs) as T
                    }
                })
                OnboardingScreen(onboardingViewModel, onFinish = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Home.route) {
                HomeScreen(homeViewModel, onRecipeClick = { id ->
                    navController.navigate("recipe/$id")
                })
            }
            composable(Screen.Grocery.route) {
                GroceryScreen(groceryViewModel)
            }
            composable(Screen.Saved.route) {
                SavedScreen(savedViewModel, onRecipeClick = { recipe ->
                    homeViewModel.recipeCache[recipe.id] = recipe
                    navController.navigate("recipe/${recipe.id}")
                })
            }
            composable(Screen.Chat.route) {
                ChatScreen(chatViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(settingsViewModel)
            }
            composable(
                route = Screen.RecipeDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                val recipe = homeViewModel.recipeCache[id] ?: return@composable
                
                val detailViewModel: RecipeDetailViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return RecipeDetailViewModel(app.openRouterClient) as T
                    }
                })
                detailViewModel.setInitialRecipe(recipe)
                
                RecipeDetailScreen(
                    viewModel = detailViewModel,
                    savedViewModel = savedViewModel,
                    onBack = { navController.popBackStack() },
                    onGetIngredients = { navController.navigate(Screen.Grocery.route) }
                )
            }
        }
    }
}
