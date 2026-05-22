package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class CompleteStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase
) {
    /** Aggregates segment totals, stamps GPS, and transitions DRAFT → COMPLETE. */
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
