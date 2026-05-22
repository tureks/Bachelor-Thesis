package cz.cvut.fel.android_app.data.measurement

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus

@Entity(tableName = "stream_measurement")
data class StreamMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val note: String?,
    @ColumnInfo(name = "measure_timestamp") val measureTimestamp: Long,
    @ColumnInfo(name = "gps_lat") val gpsLat: Double?,
    @ColumnInfo(name = "gps_long") val gpsLong: Double?,
    @ColumnInfo(name = "total_width") val totalWidth: Double?,
    @ColumnInfo(name = "max_depth") val maxDepth: Double?,
    @ColumnInfo(name = "total_flow") val totalFlow: Double?,
    val status: StreamMeasurementStatus
)