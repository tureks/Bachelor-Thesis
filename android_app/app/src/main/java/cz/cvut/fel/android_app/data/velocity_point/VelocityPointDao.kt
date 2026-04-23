package cz.cvut.fel.android_app.data.velocity_point

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VelocityPointDao {
    @Query("SELECT id, segment_id, velocity, measure_height FROM velocity_point WHERE segment_id = :segmentId")
    suspend fun getBySegmentId(segmentId: Int): List<VelocityPointEntity>

    @Insert
    suspend fun insert(point: VelocityPointEntity): Long

    @Query("DELETE FROM velocity_point WHERE segment_id = :segmentId")
    suspend fun deleteBySegmentId(segmentId: Int)
}