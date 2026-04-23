package cz.cvut.fel.android_app.data.stream_segment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface StreamSegmentDao {
    @Query("SELECT id, measurement_id, segment_number, segment_width, depth, average_velocity, segment_flow FROM stream_segment WHERE measurement_id = :measurementId ORDER BY segment_number ASC")
    suspend fun getByMeasurementId(measurementId: Int): List<StreamSegmentEntity>

    @Insert
    suspend fun insert(segment: StreamSegmentEntity): Long

    @Update
    suspend fun update(segment: StreamSegmentEntity)
}