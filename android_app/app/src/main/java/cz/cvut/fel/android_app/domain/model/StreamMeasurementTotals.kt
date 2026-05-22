package cz.cvut.fel.android_app.domain.model

/** Aggregate of all segment metrics in meters or m³/s. */
data class StreamMeasurementTotals(
    val totalWidth: Double,
    val maxDepth: Double,
    val totalFlow: Double
)
