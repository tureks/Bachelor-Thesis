package cz.cvut.fel.android_app.data.velocity_point

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class VelocityPointDao {
    @Query("SELECT id, segment_id, velocity, measure_height FROM velocity_point WHERE segment_id = :segmentId")
    abstract suspend fun getBySegmentId(segmentId: Int): List<VelocityPointEntity>

    @Insert
    abstract suspend fun insert(point: VelocityPointEntity): Long

    @Query("DELETE FROM velocity_point WHERE segment_id = :segmentId")
    abstract suspend fun deleteBySegmentId(segmentId: Int)

    @Transaction
    open suspend fun replacePoints(segmentId: Int, points: List<VelocityPointEntity>) {
        deleteBySegmentId(segmentId)
        points.forEach { insert(it) }
    }
}