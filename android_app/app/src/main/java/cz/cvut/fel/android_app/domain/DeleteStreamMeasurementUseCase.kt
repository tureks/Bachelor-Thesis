package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class DeleteStreamMeasurementUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Deletes a specific measurement by its ID.
     * Note: Deletion is cascaded to all child segments and velocity points
     */
    suspend operator fun invoke(id: Int) {
        repository.getById(id)?.let {
            repository.delete(it)
        }
    }
}
