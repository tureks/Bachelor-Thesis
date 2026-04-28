package cz.cvut.fel.android_app.ui

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object Measurement : Screen("measurement")
    object History : Screen("history")
    object Device : Screen("device")
    object Settings : Screen("settings")
}
