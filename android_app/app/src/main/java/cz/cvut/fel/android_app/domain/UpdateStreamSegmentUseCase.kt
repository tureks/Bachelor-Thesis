package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

class UpdateStreamSegmentUseCase(
    private val repository: StreamMeasurementRepository
) {
    /**
     * Updates an existing segment and its points.
     * Recalculates average velocity and flow based on the updated point data.
     */
    suspend operator fun invoke(
        segment: StreamSegment,
        updatedPoints: List<VelocityPoint>
    ) {
        // Recalculate stats based on updated point values
        val avgVelocity = if (updatedPoints.isEmpty()) 0.0 else {
            updatedPoints.sumOf { it.velocity } / updatedPoints.size
        }
        val flow = avgVelocity * segment.segmentWidth * segment.depth

        // Prepare the updated segment object
        val finalizedSegment = segment.copy(
            averageVelocity = avgVelocity,
            segmentFlow = flow
        )

        // Persist changes
        repository.updateSegment(finalizedSegment)
        
        // Replace points
        repository.deleteVelocityPoints(segment.id)
        updatedPoints.forEach { point ->
            repository.insertVelocityPoint(point.copy(segmentId = segment.id))
        }
    }
}
