package cz.cvut.fel.android_app.fixtures

import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeStreamMeasurementRepository : StreamMeasurementRepository {

    private val measurements = mutableListOf<StreamMeasurement>()
    private val segments = mutableListOf<StreamSegment>()
    private val points = mutableListOf<VelocityPoint>()

    private val measurementsFlow = MutableStateFlow<List<StreamMeasurement>>(emptyList())

    fun seed(vararg ms: StreamMeasurement) {
        measurements.addAll(ms)
        measurementsFlow.value = measurements.toList()
    }

    fun seedSegments(vararg ss: StreamSegment) = segments.addAll(ss)

    fun seedPoints(vararg ps: VelocityPoint) = points.addAll(ps)

    override fun search(query: String, fromTimestamp: Long?): Flow<List<StreamMeasurement>> =
        measurementsFlow.map { list ->
            val completed = list.filter { it.status == StreamMeasurementStatus.COMPLETE }
            val filtered = if (query.isBlank()) completed else completed.filter { m ->
                m.name.contains(query, ignoreCase = true) ||
                        m.note?.contains(query, ignoreCase = true) == true
            }
            if (fromTimestamp != null) {
                filtered.filter { it.measureTimestamp >= fromTimestamp }.sortedBy { it.measureTimestamp }
            } else {
                filtered.sortedByDescending { it.measureTimestamp }
            }
        }

    override fun getDraftFlow(): Flow<StreamMeasurement?> =
        measurementsFlow.map { it.firstOrNull { m -> m.status == StreamMeasurementStatus.DRAFT } }

    override suspend fun getDraft(): StreamMeasurement? =
        measurements.firstOrNull { it.status == StreamMeasurementStatus.DRAFT }

    override suspend fun deleteDraft() {
        measurements.removeAll { it.status == StreamMeasurementStatus.DRAFT }
        measurementsFlow.value = measurements.toList()
    }

    override suspend fun getById(id: Int): StreamMeasurement? = measurements.firstOrNull { it.id == id }

    override suspend fun deleteById(id: Int) {
        measurements.removeAll { it.id == id }
        measurementsFlow.value = measurements.toList()
    }

    override suspend fun insert(measurement: StreamMeasurement): Long {
        val id = (measurements.maxOfOrNull { it.id } ?: 0) + 1
        measurements.add(measurement.copy(id = id))
        measurementsFlow.value = measurements.toList()
        return id.toLong()
    }

    override suspend fun update(measurement: StreamMeasurement) {
        val idx = measurements.indexOfFirst { it.id == measurement.id }
        if (idx >= 0) measurements[idx] = measurement
        measurementsFlow.value = measurements.toList()
    }

    override suspend fun delete(measurement: StreamMeasurement) {
        measurements.removeAll { it.id == measurement.id }
        measurementsFlow.value = measurements.toList()
    }

    override suspend fun getSegments(measurementId: Int): List<StreamSegment> =
        segments.filter { it.measurementId == measurementId }.sortedBy { it.segmentNumber }

    override fun getSegmentsFlow(measurementId: Int): Flow<List<StreamSegment>> =
        MutableStateFlow(segments.filter { it.measurementId == measurementId })

    override suspend fun insertSegment(segment: StreamSegment): Long {
        val id = (segments.maxOfOrNull { it.id } ?: 0) + 1
        segments.add(segment.copy(id = id))
        return id.toLong()
    }

    override suspend fun updateSegment(segment: StreamSegment) {
        val idx = segments.indexOfFirst { it.id == segment.id }
        if (idx >= 0) segments[idx] = segment
    }

    override suspend fun getVelocityPoints(segmentId: Int): List<VelocityPoint> =
        points.filter { it.segmentId == segmentId }

    override suspend fun insertVelocityPoint(point: VelocityPoint): Long {
        val id = (points.maxOfOrNull { it.id } ?: 0) + 1
        points.add(point.copy(id = id))
        return id.toLong()
    }

    override suspend fun deleteVelocityPoints(segmentId: Int) =
        points.removeAll { it.segmentId == segmentId }.let {}

    override suspend fun replaceVelocityPoints(segmentId: Int, points: List<VelocityPoint>) {
        deleteVelocityPoints(segmentId)
        points.forEach { insertVelocityPoint(it) }
    }
}