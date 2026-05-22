package cz.cvut.fel.android_app.domain.model

/**
 * One cross-sectional strip of a measurement.
 *
 * @property segmentNumber segment position within the measurement
 * @property segmentWidth width in meters
 * @property depth water depth in meters
 * @property averageVelocity mean of all [VelocityPoint]s in m/s
 * @property segmentFlow partial discharge (w × d × v̄) in m³/s
 */
data class StreamSegment(
    val id: Int = 0,
    val measurementId: Int,
    val segmentNumber: Int,
    val segmentWidth: Double,
    val depth: Double,
    val averageVelocity: Double,
    val segmentFlow: Double
)