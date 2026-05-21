package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurementTotals
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class GetStreamMeasurementSummaryUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Aggregates all segments and returns totals in metric units.
     */
    suspend operator fun invoke(measurementId: Int): StreamMeasurementTotals {
        val segments = repository.getSegments(measurementId)

        if (segments.isEmpty()) {
            return StreamMeasurementTotals(0.0, 0.0, 0.0)
        }

        return StreamMeasurementTotals(
            totalWidth = segments.sumOf { it.segmentWidth },
            maxDepth = segments.maxOfOrNull { it.depth } ?: 0.0,
            totalFlow = segments.sumOf { it.segmentFlow }
        )
    }
}
