package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurementTotals
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class GetStreamMeasurementSummaryUseCase(
    private val repository: StreamMeasurementRepository
) {
    suspend operator fun invoke(measurementId: Int): StreamMeasurementTotals {
        val segments = repository.getSegments(measurementId)
        
        if (segments.isEmpty()) {
            return StreamMeasurementTotals(0.0, 0.0, 0.0, 0)
        }

        val totalWidth = segments.sumOf { it.segmentWidth }
        val maxDepth = segments.maxOf { it.depth }
        val totalFlow = segments.sumOf { it.segmentFlow }

        return StreamMeasurementTotals(
            totalWidth = totalWidth,
            maxDepth = maxDepth,
            totalFlow = totalFlow,
            segmentCount = segments.size
        )
    }
}
