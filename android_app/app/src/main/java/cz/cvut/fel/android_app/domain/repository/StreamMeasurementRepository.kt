package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import kotlinx.coroutines.flow.Flow

interface StreamMeasurementRepository {
    /**
     * Emits COMPLETE measurements matching [query] against name/note.
     * If [fromTimestamp] is set, only measurements from that epoch-ms onward are returned, ordered ascending.
     * Otherwise results are ordered newest-first.
     */
    fun search(query: String = "", fromTimestamp: Long? = null): Flow<List<StreamMeasurement>>
    /** Emits the current DRAFT measurement, or null, updating reactively. */
    fun getDraftFlow(): Flow<StreamMeasurement?>
    /** Returns the current DRAFT measurement, or null if none exists. */
    suspend fun getDraft(): StreamMeasurement?
    /** Deletes the current DRAFT measurement if one exists. */
    suspend fun deleteDraft()
    suspend fun getById(id: Int): StreamMeasurement?
    suspend fun deleteById(id: Int)
    /** Inserts [measurement] and returns the generated row ID. */
    suspend fun insert(measurement: StreamMeasurement): Long
    suspend fun update(measurement: StreamMeasurement)
    suspend fun delete(measurement: StreamMeasurement)
    /** Returns segments for [measurementId] sorted by segment number. */
    suspend fun getSegments(measurementId: Int): List<StreamSegment>
    fun getSegmentsFlow(measurementId: Int): Flow<List<StreamSegment>>
    /** Inserts [segment] and returns the generated row ID. */
    suspend fun insertSegment(segment: StreamSegment): Long
    suspend fun updateSegment(segment: StreamSegment)
    suspend fun getVelocityPoints(segmentId: Int): List<VelocityPoint>
    /** Inserts [point] and returns the generated row ID. */
    suspend fun insertVelocityPoint(point: VelocityPoint): Long
    suspend fun deleteVelocityPoints(segmentId: Int)
}