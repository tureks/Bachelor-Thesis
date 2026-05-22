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
        repository.getDraft()?.let {
            repository.delete(it)
        }

        val newMeasurement = StreamMeasurement(
            name = name,
            note = null,
            measureTimestamp = System.currentTimeMillis(),
            gpsLat = null,
            gpsLong = null,
            totalWidth = null,
            maxDepth = null,
            totalFlow = null,
            status = StreamMeasurementStatus.DRAFT
        )

        return repository.insert(newMeasurement)
    }
}
