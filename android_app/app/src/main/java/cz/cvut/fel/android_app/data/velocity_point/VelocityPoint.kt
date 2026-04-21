package cz.cvut.fel.android_app.data.velocity_point

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cz.cvut.fel.android_app.data.stream_segment.StreamSegment

@Entity(
    tableName = "velocity_point",
    foreignKeys = [
        ForeignKey(
            entity = StreamSegment::class,
            parentColumns = ["id"],
            childColumns = ["segment_id"],
            onDelete = ForeignKey.Companion.CASCADE
        )
    ],
    indices = [Index("segment_id")]
)
data class VelocityPoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "segment_id") val segmentId: Int,
    val velocity: Double,
    @ColumnInfo(name = "measure_height") val measureHeight: Double?
)