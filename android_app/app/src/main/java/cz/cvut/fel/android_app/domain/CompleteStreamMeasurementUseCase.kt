package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class CompleteStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase
) {
    /**
     * Finalizes the entire stream measurement session.
     * Takes user metadata, calculates final aggregates, and marks as COMPLETE.
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
        
        // Calculate the final river totals from all recorded segments
        val summary = getSummaryUseCase(measurementId)

        // Create the finalized domain object
        val finalizedMeasurement = measurement.copy(
            name = name,
            location = location, // User-editable string
            note = note,
            gpsLat = gpsLat,
            gpsLong = gpsLong,
            totalWidth = summary.totalWidth,
            maxDepth = summary.maxDepth,
            totalFlow = summary.totalFlow,
            status = StreamMeasurementStatus.COMPLETE
        )

        // Persist to database
        repository.update(finalizedMeasurement)
    }
}
