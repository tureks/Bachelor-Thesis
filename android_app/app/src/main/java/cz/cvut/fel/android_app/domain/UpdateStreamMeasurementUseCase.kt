package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class UpdateStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Updates the metadata of a stream measurement.
     */
    suspend operator fun invoke(
        measurementId: Int,
        name: String,
        location: String?,
        note: String?
    ) {
        val measurement = repository.getById(measurementId) ?: return
        
        val updatedMeasurement = measurement.copy(
            name = name,
            location = location,
            note = note
        )
        
        repository.update(updatedMeasurement)
    }
}
