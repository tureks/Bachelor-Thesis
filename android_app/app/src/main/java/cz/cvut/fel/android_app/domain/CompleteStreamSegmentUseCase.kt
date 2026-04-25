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
     * Finalizes the segment by saving it and its associated points to the database.
     * Automatically determines the next segment number and handles manual point overrides.
     */
    suspend operator fun invoke(
        measurementId: Int,
        segmentWidth: Double,
        depth: Double,
        points: List<CapturedVelocityPoint>,
        selectedIndices: Set<Int>
    ) {
        // Determine the next segment number based on existing count
        val existingSegments = repository.getSegments(measurementId)
        val nextNumber = existingSegments.size + 1

        // Get the final calculation
        val result = calculateUseCase(segmentWidth, depth, points, selectedIndices)

        // Map to the persistence model (StreamSegment)
        val segment = StreamSegment(
            measurementId = measurementId,
            segmentNumber = nextNumber,
            segmentWidth = result.segmentWidth,
            depth = result.depth,
            averageVelocity = result.averageVelocity,
            segmentFlow = result.segmentFlow
        )

        // Save the segment
        val segmentId = repository.insertSegment(segment).toInt()

        // Save only the selected velocity points associated with this segment
        result.selectedPoints.forEach { captured ->
            val point = VelocityPoint(
                segmentId = segmentId,
                velocity = captured.velocity,
                measureHeight = captured.measureHeight
            )
            repository.insertVelocityPoint(point)
        }
    }
}
