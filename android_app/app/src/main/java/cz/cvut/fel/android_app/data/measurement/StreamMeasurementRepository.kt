package cz.cvut.fel.android_app.data.measurement

import cz.cvut.fel.android_app.data.stream_segment.StreamSegment
import cz.cvut.fel.android_app.data.velocity_point.VelocityPoint
import kotlinx.coroutines.flow.Flow

interface StreamMeasurementRepository {
    fun getCompleted(): Flow<List<StreamMeasurement>>
    suspend fun getDraft(): StreamMeasurement?
    suspend fun getById(id: Int): StreamMeasurement?
    suspend fun insert(measurement: StreamMeasurement): Long
    suspend fun update(measurement: StreamMeasurement)
    suspend fun delete(measurement: StreamMeasurement)
    suspend fun getSegments(measurementId: Int): List<StreamSegment>
    suspend fun insertSegment(segment: StreamSegment): Long
    suspend fun updateSegment(segment: StreamSegment)
    suspend fun getVelocityPoints(segmentId: Int): List<VelocityPoint>
    suspend fun insertVelocityPoint(point: VelocityPoint): Long
    suspend fun deleteVelocityPoints(segmentId: Int)
}