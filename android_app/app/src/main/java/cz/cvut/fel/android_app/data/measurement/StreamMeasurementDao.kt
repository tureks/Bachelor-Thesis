package cz.cvut.fel.android_app.data.measurement

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StreamMeasurementDao {
    @Query("SELECT id, name, note, measure_timestamp, gps_lat, gps_long, total_width, max_depth, total_flow, status FROM stream_measurement WHERE status = 'COMPLETE' ORDER BY measure_timestamp DESC")
    fun getCompleted(): Flow<List<StreamMeasurementEntity>>

    @Query("SELECT id, name, note, measure_timestamp, gps_lat, gps_long, total_width, max_depth, total_flow, status FROM stream_measurement WHERE status = 'DRAFT' LIMIT 1")
    fun getDraftFlow(): Flow<StreamMeasurementEntity?>

    @Query("SELECT id, name, note, measure_timestamp, gps_lat, gps_long, total_width, max_depth, total_flow, status FROM stream_measurement WHERE status = 'DRAFT' LIMIT 1")
    suspend fun getDraft(): StreamMeasurementEntity?

    @Query("DELETE FROM stream_measurement WHERE status = 'DRAFT'")
    suspend fun deleteDraft()

    @Query("DELETE FROM stream_measurement WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT id, name, note, measure_timestamp, gps_lat, gps_long, total_width, max_depth, total_flow, status FROM stream_measurement WHERE id = :id")
    suspend fun getById(id: Int): StreamMeasurementEntity?

    @Insert
    suspend fun insert(measurement: StreamMeasurementEntity): Long

    @Update
    suspend fun update(measurement: StreamMeasurementEntity)

    @Delete
    suspend fun delete(measurement: StreamMeasurementEntity)
}
