package cz.cvut.fel.android_app.ui

sealed class Screen(val route: String) {
    data object Main : Screen("main_screen")
    data object Measurement : Screen("measurement")
    data object CompleteSegment : Screen("complete_segment")
    data object ReviewSegments : Screen("review_segments")
    data object FinalizeMeasurement : Screen("finalize_measurement")
    data object History : Screen("history")
    data object MeasurementDetails : Screen("measurement_details/{measurementId}") {
        fun createRoute(id: Int) = "measurement_details/$id"
    }
    data object Device : Screen("device")
    data object Settings : Screen("settings")
}
