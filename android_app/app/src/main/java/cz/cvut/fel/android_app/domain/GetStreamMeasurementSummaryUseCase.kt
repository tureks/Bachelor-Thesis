package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamMeasurementTotals
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.first

class GetStreamMeasurementSummaryUseCase(
    private val repository: StreamMeasurementRepository,
    private val userRepository: UserRepository
) {
    /**
     * Aggregates all segments.
     * Returns values converted to the user's preferred units (cm/L or m/m3).
     */
    suspend operator fun invoke(measurementId: Int): StreamMeasurementTotals {
        val segments = repository.getSegments(measurementId)
        val user = userRepository.user.first()
        val isHydrometric = user?.preferredUnit == MeasurementUnit.HYDROMETRIC
        
        if (segments.isEmpty()) {
            return StreamMeasurementTotals(0.0, 0.0, 0.0, 0)
        }

        val totalWidthM = segments.sumOf { it.segmentWidth }
        val maxDepthM = segments.maxOf { it.depth }
        val totalFlowM3 = segments.sumOf { it.segmentFlow }

        return if (isHydrometric) {
            StreamMeasurementTotals(
                totalWidth = totalWidthM * 100.0,
                maxDepth = maxDepthM * 100.0,
                totalFlow = totalFlowM3 * 1000.0,
                segmentCount = segments.size
            )
        } else {
            StreamMeasurementTotals(
                totalWidth = totalWidthM,
                maxDepth = maxDepthM,
                totalFlow = totalFlowM3,
                segmentCount = segments.size
            )
        }
    }
}
