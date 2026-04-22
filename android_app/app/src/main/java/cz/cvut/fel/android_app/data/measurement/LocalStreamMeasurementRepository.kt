package cz.cvut.fel.android_app.data.measurement

import cz.cvut.fel.android_app.data.stream_segment.StreamSegment
import cz.cvut.fel.android_app.data.stream_segment.StreamSegmentDao
import cz.cvut.fel.android_app.data.velocity_point.VelocityPoint
import cz.cvut.fel.android_app.data.velocity_point.VelocityPointDao
import kotlinx.coroutines.flow.Flow

class LocalStreamMeasurementRepository(
    private val measurementDao: StreamMeasurementDao,
    private val segmentDao: StreamSegmentDao,
    private val velocityPointDao: VelocityPointDao
) : StreamMeasurementRepository {

    override fun getCompleted(): Flow<List<StreamMeasurement>> = measurementDao.getCompleted()

    override suspend fun getDraft(): StreamMeasurement? = measurementDao.getDraft()

    override suspend fun getById(id: Int): StreamMeasurement? = measurementDao.getById(id)

    override suspend fun insert(measurement: StreamMeasurement): Long =
        measurementDao.insert(measurement)

    override suspend fun update(measurement: StreamMeasurement) = measurementDao.update(measurement)

    override suspend fun delete(measurement: StreamMeasurement) = measurementDao.delete(measurement)

    override suspend fun getSegments(measurementId: Int): List<StreamSegment> =
        segmentDao.getByMeasurementId(measurementId)

    override suspend fun insertSegment(segment: StreamSegment): Long = segmentDao.insert(segment)

    override suspend fun updateSegment(segment: StreamSegment) = segmentDao.update(segment)

    override suspend fun getVelocityPoints(segmentId: Int): List<VelocityPoint> =
        velocityPointDao.getBySegmentId(segmentId)

    override suspend fun insertVelocityPoint(point: VelocityPoint): Long =
        velocityPointDao.insert(point)

    override suspend fun deleteVelocityPoints(segmentId: Int) =
        velocityPointDao.deleteBySegmentId(segmentId)
}