package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import kotlinx.coroutines.flow.Flow

class SearchMeasurementsUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Searches completed measurements in the database by name/note.
     * When [fromTimestamp] is set, results start from that epoch-ms, ordered ascending. Without it, results ordered newest-first.
     */
    operator fun invoke(query: String = "", fromTimestamp: Long? = null): Flow<List<StreamMeasurement>> =
        repository.search(query, fromTimestamp)
}