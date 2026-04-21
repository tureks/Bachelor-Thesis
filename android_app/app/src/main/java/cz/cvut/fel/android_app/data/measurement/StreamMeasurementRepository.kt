package cz.cvut.fel.android_app.data.measurement

import cz.cvut.fel.android_app.data.stream_segment.StreamSegment
import cz.cvut.fel.android_app.data.stream_segment.StreamSegmentDao
import cz.cvut.fel.android_app.data.velocity_point.VelocityPoint
import cz.cvut.fel.android_app.data.velocity_point.VelocityPointDao
import kotlinx.coroutines.flow.Flow

class StreamMeasurementRepository(
    private val measurementDao: StreamMeasurementDao,
    private val segmentDao: StreamSegmentDao,
    private val velocityPointDao: VelocityPointDao
) {
    fun getCompleted(): Flow<List<StreamMeasurement>> = measurementDao.getCompleted()

    suspend fun getDraft(): StreamMeasurement? = measurementDao.getDraft()

    suspend fun getById(id: Int): StreamMeasurement? = measurementDao.getById(id)

    suspend fun insert(measurement: StreamMeasurement): Long = measurementDao.insert(measurement)

    suspend fun update(measurement: StreamMeasurement) = measurementDao.update(measurement)

    suspend fun delete(measurement: StreamMeasurement) = measurementDao.delete(measurement)

    suspend fun getSegments(measurementId: Int): List<StreamSegment> =
        segmentDao.getByMeasurementId(measurementId)

    suspend fun insertSegment(segment: StreamSegment): Long = segmentDao.insert(segment)

    suspend fun updateSegment(segment: StreamSegment) = segmentDao.update(segment)

    suspend fun getVelocityPoints(segmentId: Int): List<VelocityPoint> =
        velocityPointDao.getBySegmentId(segmentId)

    suspend fun insertVelocityPoint(point: VelocityPoint): Long =
        velocityPointDao.insert(point)

    suspend fun deleteVelocityPoints(segmentId: Int) =
        velocityPointDao.deleteBySegmentId(segmentId)
}
