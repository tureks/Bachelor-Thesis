package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchMeasurementsUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Searches for completed measurements by name or note.
     * If query is empty, returns all completed measurements.
     */
    operator fun invoke(query: String = ""): Flow<List<StreamMeasurement>> {
        return repository.getCompleted().map { list ->
            if (query.isBlank()) {
                list.sortedByDescending { it.measureTimestamp }
            } else {
                list.filter { 
                    it.name.contains(query, ignoreCase = true) || 
                    (it.note?.contains(query, ignoreCase = true) ?: false)
                }.sortedByDescending { it.measureTimestamp }
            }
        }
    }
}
