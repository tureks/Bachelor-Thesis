package cz.cvut.fel.android_app.data.measurement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cz.cvut.fel.android_app.data.device.Device

@Entity(
    tableName = "stream_measurement",
    foreignKeys = [
        ForeignKey(
            entity = Device::class,
            parentColumns = ["id"],
            childColumns = ["device_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("device_id")]
)
data class StreamMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "reference_model") val referenceModel: Int,
    val name: String,
    val note: String?,
    val location: String?,
    @ColumnInfo(name = "measure_timestamp") val measureTimestamp: Long,
    @ColumnInfo(name = "gps_lat") val gpsLat: Double?,
    @ColumnInfo(name = "gps_long") val gpsLong: Double?,
    @ColumnInfo(name = "total_width") val totalWidth: Double?,
    @ColumnInfo(name = "max_depth") val maxDepth: Double?,
    @ColumnInfo(name = "total_flow") val totalFlow: Double?,
    @ColumnInfo(name = "device_id") val deviceId: Int?,
    val status: StreamMeasurementStatus
)