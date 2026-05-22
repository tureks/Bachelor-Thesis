package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class UpdateStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository
) {
    suspend operator fun invoke(
        measurementId: Int,
        name: String,
        note: String?
    ) {
        val measurement = repository.getById(measurementId) ?: return
        repository.update(
            measurement.copy(
                name = name,
                note = note
            )
        )
    }
}
