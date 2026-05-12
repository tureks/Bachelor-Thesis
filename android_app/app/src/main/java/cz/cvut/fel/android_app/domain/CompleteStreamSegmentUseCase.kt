package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.CapturedVelocityPoint
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class CompleteStreamSegmentUseCase(
    private val repository: StreamMeasurementRepository,
    private val calculateUseCase: CalculateStreamSegmentUseCase
) {

    /**
     * Handles completing and saving segment.
     * @param segmentWidth width in meters
     * @param depth depth in meters
     */
    suspend operator fun invoke(
        measurementId: Int,
        segmentWidth: Double,
        depth: Double,
        points: List<CapturedVelocityPoint>
    ) {
        val existingSegments = repository.getSegments(measurementId)
        val nextNumber = existingSegments.size + 1

        val result = calculateUseCase(segmentWidth, depth, points)

        val segment = StreamSegment(
            measurementId = measurementId,
            segmentNumber = nextNumber,
            segmentWidth = result.segmentWidth,
            depth = result.depth,
            averageVelocity = result.averageVelocity,
            segmentFlow = result.segmentFlow
        )

        val segmentId = repository.insertSegment(segment).toInt()

        points.forEach { captured ->
            val point = VelocityPoint(
                segmentId = segmentId,
                velocity = captured.velocity,
                measureHeight = captured.measureHeight
            )
            repository.insertVelocityPoint(point)
        }
    }
}
