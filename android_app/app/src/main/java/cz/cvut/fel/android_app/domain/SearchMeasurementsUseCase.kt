package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchMeasurementsUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Searches completed measurements by name/note and optional timestamp range.
     * [fromTimestamp] and [toTimestamp] are epoch-ms; both inclusive.
     */
    operator fun invoke(
        query: String = "",
        fromTimestamp: Long? = null,
        toTimestamp: Long? = null
    ): Flow<List<StreamMeasurement>> {
        return repository.getCompleted().map { list ->
            list.filter { m ->
                val matchesText = query.isBlank() ||
                        m.name.contains(query, ignoreCase = true) ||
                        (m.note?.contains(query, ignoreCase = true) == true)
                val afterFrom = fromTimestamp == null || m.measureTimestamp >= fromTimestamp
                val beforeTo = toTimestamp == null || m.measureTimestamp <= toTimestamp
                matchesText && afterFrom && beforeTo
            }.let { filtered ->
                if (fromTimestamp != null) filtered.sortedBy { it.measureTimestamp }
                else filtered.sortedByDescending { it.measureTimestamp }
            }
        }
    }
}
