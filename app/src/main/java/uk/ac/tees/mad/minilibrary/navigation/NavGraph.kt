package uk.ac.tees.mad.minilibrary.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import uk.ac.tees.mad.minilibrary.screens.AuthScreen

import uk.ac.tees.mad.minilibrary.screens.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Authentication Screen
        composable(Screen.Auth.route) {
            AuthScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(Screen.Home.route) {
//            HomeScreen(
//                onNavigateToReader = { bookId ->
//                    navController.navigate(Screen.BookReader.createRoute(bookId))
//                },
//                onNavigateToSettings = {
//                    navController.navigate(Screen.Settings.route)
//                }
//            )
        }

        // Book Reader Screen (with argument)
        composable(
            route = Screen.BookReader.route,
            arguments = listOf(
                navArgument("bookId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
//            BookReaderScreen(
//                bookId = bookId,
//                onNavigateBack = {
//                    navController.popBackStack()
//                }
//            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
//            SettingsScreen(
//                onNavigateBack = {
//                    navController.popBackStack()
//                },
//                onLogout = {
//                    navController.navigate(Screen.Auth.route) {
//                        popUpTo(0) { inclusive = true }
//                    }
//                }
//            )
        }
    }
}