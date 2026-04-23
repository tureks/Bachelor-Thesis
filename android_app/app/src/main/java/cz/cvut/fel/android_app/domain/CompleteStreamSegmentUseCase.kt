package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.CapturedVelocityPoint
import cz.cvut.fel.android_app.domain.model.StreamSegmentResult

class CompleteStreamSegmentUseCase {

    operator fun invoke(
        capturedPoints: List<CapturedVelocityPoint>,
        selectedIndices: Set<Int>,
        segmentWidth: Double,
        depth: Double
    ): StreamSegmentResult? {
        val selected = capturedPoints.filterIndexed { index, _ -> index in selectedIndices }
        if (selected.isEmpty()) return null

        val averageVelocity = selected.sumOf { it.velocity } / selected.size

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