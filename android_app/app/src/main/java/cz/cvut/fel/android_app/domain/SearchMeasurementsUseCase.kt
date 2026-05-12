package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchMeasurementsUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Searches completed measurements by name/note with an optional start date.
     * Results are sorted ascending by timestamp when [fromTimestamp] is set, descending otherwise.
     * @param query filter string matched against name and note
     * @param fromTimestamp lower bound in epoch-ms, shows measurements from that day to present
     */
    operator fun invoke(
        query: String = "",
        fromTimestamp: Long? = null
    ): Flow<List<StreamMeasurement>> {
        return repository.getCompleted().map { list ->
            list.filter { m ->
                val matchesText = query.isBlank() ||
                        m.name.contains(query, ignoreCase = true) ||
                        (m.note?.contains(query, ignoreCase = true) == true)
                val afterFrom = fromTimestamp == null || m.measureTimestamp >= fromTimestamp
                matchesText && afterFrom
            }.let { filtered ->
                if (fromTimestamp != null) filtered.sortedBy { it.measureTimestamp }
                else filtered.sortedByDescending { it.measureTimestamp }
            }
        }
    }
}
