package cz.cvut.fel.android_app.domain.model

data class StreamMeasurementTotals(
    val totalWidth: Double,
    val maxDepth: Double,
    val totalFlow: Double,
    val segmentCount: Int,
    val minVelocity: Double = 0.0,
    val maxVelocity: Double = 0.0
)
