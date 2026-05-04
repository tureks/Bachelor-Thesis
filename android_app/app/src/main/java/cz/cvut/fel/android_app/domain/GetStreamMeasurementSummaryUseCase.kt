package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamMeasurementTotals
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class GetStreamMeasurementSummaryUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Aggregates all segments and returns totals in metric units (m, m³/s).
     * Callers are responsible for display-unit conversion.
     */
    suspend operator fun invoke(measurementId: Int): StreamMeasurementTotals {
        val segments = repository.getSegments(measurementId)

        if (segments.isEmpty()) {
            return StreamMeasurementTotals(0.0, 0.0, 0.0, 0)
        }

        val allVelocities = segments.flatMap { repository.getVelocityPoints(it.id).map { p -> p.velocity } }

        return StreamMeasurementTotals(
            totalWidth = segments.sumOf { it.segmentWidth },
            maxDepth = segments.maxOf { it.depth },
            totalFlow = segments.sumOf { it.segmentFlow },
            segmentCount = segments.size,
            minVelocity = allVelocities.minOrNull() ?: 0.0,
            maxVelocity = allVelocities.maxOrNull() ?: 0.0
        )
    }
}
