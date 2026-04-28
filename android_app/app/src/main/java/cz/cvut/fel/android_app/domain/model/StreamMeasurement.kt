package cz.cvut.fel.android_app.domain.model

data class StreamMeasurement(
    val id: Int = 0,
    val referenceModel: Int,
    val name: String,
    val note: String?,
    val measureTimestamp: Long,
    val gpsLat: Double?,
    val gpsLong: Double?,
    val totalWidth: Double?,
    val maxDepth: Double?,
    val totalFlow: Double?,
    val deviceId: Int?,
    val status: StreamMeasurementStatus
)