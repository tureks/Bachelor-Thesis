package cz.cvut.fel.android_app.domain.model

/** Output of [CalculateStreamSegmentUseCase]. All values in SI units (m, m/s, m³/s). */
data class StreamSegmentResult(
    val segmentWidth: Double,
    val depth: Double,
    val averageVelocity: Double,
    val segmentFlow: Double
)