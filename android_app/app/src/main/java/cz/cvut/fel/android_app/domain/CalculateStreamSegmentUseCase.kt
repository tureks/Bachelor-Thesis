package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.CapturedVelocityPoint
import cz.cvut.fel.android_app.domain.model.StreamSegmentResult

class CalculateStreamSegmentUseCase {

    operator fun invoke(
        segmentWidth: Double,
        depth: Double,
        points: List<CapturedVelocityPoint>,
        selectedIndices: Set<Int>
    ): StreamSegmentResult {
        val selected = points.filterIndexed { index, _ -> index in selectedIndices }

        val averageVelocity = if (selected.isEmpty()) 0.0 else {
            selected.sumOf { it.velocity } / selected.size
        }

        // Mid-section method: Q = V_avg × width × depth
        val segmentFlow = averageVelocity * segmentWidth * depth

        return StreamSegmentResult(
            segmentWidth = segmentWidth,
            depth = depth,
            averageVelocity = averageVelocity,
            segmentFlow = segmentFlow,
            selectedPoints = selected
        )
    }
}
