package uk.ac.tees.mad.minilibrary.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object BookReader : Screen("book_reader/{bookId}") {
        fun createRoute(bookId: String) = "book_reader/$bookId"
    }
    object Settings : Screen("settings")
}