package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    /** Hot flow of GPS updates (~5 s / 1 m interval). */
    fun observeLocation(): Flow<Location?>
    /** One-shot last known location. */
    suspend fun getCurrentLocation(): Location?
}
