package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.Location
import cz.cvut.fel.android_app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow

class ObserveLocationUseCase(
    private val locationRepository: LocationRepository
) {
    /**
     * Returns a stream of GPS locations.
     */
    operator fun invoke(): Flow<Location?> = locationRepository.observeLocation()
}
