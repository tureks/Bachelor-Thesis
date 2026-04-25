package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class ContinueStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Returns the ID of the existing draft measurement.
     * Returns null if no draft exists.
     */
    suspend operator fun invoke(): Long? {
        return repository.getDraft()?.id?.toLong()
    }
}