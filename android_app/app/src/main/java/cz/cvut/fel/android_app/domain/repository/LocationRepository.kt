package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    /**
     * Returns a flow of location updates.
     */
    fun observeLocation(): Flow<Location?>

    /**
     * Returns the last known location once.
     */
    suspend fun getCurrentLocation(): Location?
}
