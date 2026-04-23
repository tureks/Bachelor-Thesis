package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
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