package cz.cvut.fel.android_app.domain.model

data class StreamSegment(
    val id: Int = 0,
    val measurementId: Int,
    val segmentNumber: Int,
    val segmentWidth: Double,
    val depth: Double,
    val averageVelocity: Double,
    val segmentFlow: Double
)