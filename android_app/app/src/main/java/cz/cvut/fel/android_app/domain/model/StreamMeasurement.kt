package cz.cvut.fel.android_app.domain.model

/**
 * Top-level record for a stream measurement.
 *
 * @property measureTimestamp epoch milliseconds
 * @property gpsLat WGS-84 latitude
 * @property gpsLong WGS-84 longitude
 * @property totalWidth sum of segment widths in meters
 * @property maxDepth deepest segment in meters
 * @property totalFlow total discharge in m³/s
 */
data class StreamMeasurement(
    val id: Int = 0,
    val name: String,
    val note: String?,
    val measureTimestamp: Long,
    val gpsLat: Double?,
    val gpsLong: Double?,
    val totalWidth: Double?,
    val maxDepth: Double?,
    val totalFlow: Double?,
    val status: StreamMeasurementStatus
)