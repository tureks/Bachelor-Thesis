package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class StartStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Deletes any existing draft and starts a new measurement.
     */
    suspend operator fun invoke(name: String = "New Measurement"): Long {
        // Delete existing draft if any
        repository.getDraft()?.let {
            repository.delete(it)
        }

        // Create a new draft
        val newMeasurement = StreamMeasurement(
            referenceModel = 0,
            name = name,
            note = null,
            measureTimestamp = System.currentTimeMillis(),
            gpsLat = null,
            gpsLong = null,
            totalWidth = null,
            maxDepth = null,
            totalFlow = null,
            deviceId = null,
            status = StreamMeasurementStatus.DRAFT
        )

        return repository.insert(newMeasurement)
    }
}
