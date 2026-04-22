package cz.cvut.fel.android_app.data.stream_segment

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cz.cvut.fel.android_app.data.measurement.StreamMeasurement

@Entity(
    tableName = "stream_segment",
    foreignKeys = [
        ForeignKey(
            entity = StreamMeasurement::class,
            parentColumns = ["id"],
            childColumns = ["measurement_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("measurement_id")]
)
data class StreamSegment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "measurement_id") val measurementId: Int,
    @ColumnInfo(name = "segment_number") val segmentNumber: Int,
    @ColumnInfo(name = "segment_width") val segmentWidth: Double,
    val depth: Double,
    @ColumnInfo(name = "average_velocity") val averageVelocity: Double,
    @ColumnInfo(name = "segment_flow") val segmentFlow: Double
)