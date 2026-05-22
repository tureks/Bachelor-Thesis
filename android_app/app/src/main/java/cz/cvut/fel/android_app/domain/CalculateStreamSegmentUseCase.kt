package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.CapturedVelocityPoint
import cz.cvut.fel.android_app.domain.model.StreamSegmentResult

/** Computes partial discharge. */
class CalculateStreamSegmentUseCase {

    /**
     * @param segmentWidth width in meters
     * @param depth depth in meters
     * @param points velocity readings for this strip
     * @return [StreamSegmentResult] with flow in m³/s
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
