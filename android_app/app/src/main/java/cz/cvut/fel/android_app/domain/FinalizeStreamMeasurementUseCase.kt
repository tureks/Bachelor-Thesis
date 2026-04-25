package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class FinalizeStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase
) {
    /**
     * Finalizes the entire measurement. 
     * Updates metadata, calculates final totals, and marks status as COMPLETE.
     */
    suspend operator fun invoke(
        measurementId: Int,
        name: String,
        location: String,
        note: String,
        gpsLat: Double?,
        gpsLong: Double?
    ) {
        val measurement = repository.getById(measurementId) ?: return
        val summary = getSummaryUseCase(measurementId)

        val finalizedMeasurement = measurement.copy(
            name = name,
            location = location,
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
