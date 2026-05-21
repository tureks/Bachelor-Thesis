package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.CapturedVelocityPoint
import cz.cvut.fel.android_app.domain.model.StreamSegmentResult

class CalculateStreamSegmentUseCase {

    /**
     * Counts flow based on velocity points using mid-section method.
     * @param segmentWidth segment width in meters
     * @param depth water depth in meters
     * @param points captured velocity points for this segment
     * @return [StreamSegmentResult] with computed flow in m³/s
     */
    operator fun invoke(
        segmentWidth: Double,
        depth: Double,
        points: List<CapturedVelocityPoint>
    ): StreamSegmentResult {
        val averageVelocity = if (points.isEmpty()) 0.0 else {
            points.sumOf { it.velocity } / points.size
        }

        val segmentFlow = averageVelocity * segmentWidth * depth

        return StreamSegmentResult(
            segmentWidth = segmentWidth,
            depth = depth,
            averageVelocity = averageVelocity,
            segmentFlow = segmentFlow
        )
    }
}
