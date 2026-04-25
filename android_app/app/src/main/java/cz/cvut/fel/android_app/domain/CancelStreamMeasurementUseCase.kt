package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class CancelStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Discards the current draft measurement.
     */
    suspend operator fun invoke() {
        repository.getDraft()?.let {
            repository.delete(it)
        }
    }
}