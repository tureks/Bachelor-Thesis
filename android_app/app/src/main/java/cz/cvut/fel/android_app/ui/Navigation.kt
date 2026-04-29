package cz.cvut.fel.android_app.ui

sealed class Screen(val route: String) {
    data object Main : Screen("main_screen")
    data object Measurement : Screen("measurement")
    data object FinalizeMeasurement : Screen("finalize_measurement")
    data object CompleteSegment : Screen("complete_segment")
    data object History : Screen("history")
    data object Device : Screen("device")
    data object Settings : Screen("settings")
}
