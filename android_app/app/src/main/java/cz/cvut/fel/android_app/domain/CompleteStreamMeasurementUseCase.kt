package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class CompleteStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase
) {
    /**
     * Finalizes the entire stream measurement session.
     * name: The primary identifier (e.g. Station Name, River, or Location).
     */
    suspend operator fun invoke(
        measurementId: Int,
        name: String,
        note: String? = null,
        gpsLat: Double? = null,
        gpsLong: Double? = null
    ) {
        val measurement = repository.getById(measurementId) ?: return
        
        val summary = getSummaryUseCase(measurementId)

        val finalizedMeasurement = measurement.copy(
            name = name,
            location = null, // Location field is deprecated in favor of name + GPS
            note = note,
            gpsLat = gpsLat,
            gpsLong = gpsLong,
            totalWidth = summary.totalWidth,
            maxDepth = summary.maxDepth,
            totalFlow = summary.totalFlow,
            status = StreamMeasurementStatus.COMPLETE
        )

        repository.update(finalizedMeasurement)
    }
}
