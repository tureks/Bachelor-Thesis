package cz.cvut.fel.android_app.domain.model

data class StreamSegmentResult(
    val segmentWidth: Double,
    val depth: Double,
    val averageVelocity: Double,
    val segmentFlow: Double,
    val selectedPoints: List<CapturedVelocityPoint>
)