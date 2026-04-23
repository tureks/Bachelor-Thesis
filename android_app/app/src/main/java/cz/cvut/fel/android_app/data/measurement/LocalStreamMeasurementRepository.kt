package cz.cvut.fel.android_app.data.measurement

import cz.cvut.fel.android_app.data.stream_segment.StreamSegmentDao
import cz.cvut.fel.android_app.data.stream_segment.StreamSegmentEntity
import cz.cvut.fel.android_app.data.velocity_point.VelocityPointDao
import cz.cvut.fel.android_app.data.velocity_point.VelocityPointEntity
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalStreamMeasurementRepository(
    private val measurementDao: StreamMeasurementDao,
    private val segmentDao: StreamSegmentDao,
    private val velocityPointDao: VelocityPointDao
) : StreamMeasurementRepository {

    override fun getCompleted(): Flow<List<StreamMeasurement>> =
        measurementDao.getCompleted().map { list -> list.map { it.toDomain() } }

    override suspend fun getDraft(): StreamMeasurement? =
        measurementDao.getDraft()?.toDomain()

    override suspend fun getById(id: Int): StreamMeasurement? =
        measurementDao.getById(id)?.toDomain()

    override suspend fun insert(measurement: StreamMeasurement): Long =
        measurementDao.insert(measurement.toEntity())

    override suspend fun update(measurement: StreamMeasurement) =
        measurementDao.update(measurement.toEntity())

    override suspend fun delete(measurement: StreamMeasurement) =
        measurementDao.delete(measurement.toEntity())

    override suspend fun getSegments(measurementId: Int): List<StreamSegment> =
        segmentDao.getByMeasurementId(measurementId).map { it.toDomain() }

    override suspend fun insertSegment(segment: StreamSegment): Long =
        segmentDao.insert(segment.toEntity())

    override suspend fun updateSegment(segment: StreamSegment) =
        segmentDao.update(segment.toEntity())

    override suspend fun getVelocityPoints(segmentId: Int): List<VelocityPoint> =
        velocityPointDao.getBySegmentId(segmentId).map { it.toDomain() }

    override suspend fun insertVelocityPoint(point: VelocityPoint): Long =
        velocityPointDao.insert(point.toEntity())

    override suspend fun deleteVelocityPoints(segmentId: Int) =
        velocityPointDao.deleteBySegmentId(segmentId)
}

private fun StreamMeasurementEntity.toDomain() = StreamMeasurement(
    id, referenceModel, name, note, location, measureTimestamp,
    gpsLat, gpsLong, totalWidth, maxDepth, totalFlow, deviceId, status
)

private fun StreamMeasurement.toEntity() = StreamMeasurementEntity(
    id, referenceModel, name, note, location, measureTimestamp,
    gpsLat, gpsLong, totalWidth, maxDepth, totalFlow, deviceId, status
)

private fun StreamSegmentEntity.toDomain() =
    StreamSegment(id, measurementId, segmentNumber, segmentWidth, depth, averageVelocity, segmentFlow)

private fun StreamSegment.toEntity() =
    StreamSegmentEntity(id, measurementId, segmentNumber, segmentWidth, depth, averageVelocity, segmentFlow)

private fun VelocityPointEntity.toDomain() = VelocityPoint(id, segmentId, velocity, measureHeight)
private fun VelocityPoint.toEntity() = VelocityPointEntity(id, segmentId, velocity, measureHeight)
