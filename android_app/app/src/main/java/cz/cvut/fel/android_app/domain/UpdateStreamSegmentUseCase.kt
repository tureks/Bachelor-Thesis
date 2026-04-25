package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.first

class UpdateStreamSegmentUseCase(
    private val repository: StreamMeasurementRepository,
    private val userRepository: UserRepository
) {
    /**
     * Updates an existing segment and its points.
     * Inputs (segment.segmentWidth, segment.depth) are expected in user's preferred unit (cm/m).
     * This use case ensures they are stored in Metric (m) and flow is calculated correctly.
     */
    suspend operator fun invoke(
        segment: StreamSegment,
        updatedPoints: List<VelocityPoint>
    ) {
        val user = userRepository.user.first()
        val isHydrometric = user?.preferredUnit == MeasurementUnit.HYDROMETRIC

        // Convert UI inputs to Metric (m) if they were edited in cm
        val widthMetric = if (isHydrometric) segment.segmentWidth / 100.0 else segment.segmentWidth
        val depthMetric = if (isHydrometric) segment.depth / 100.0 else segment.depth

        // Recalculate stats based on updated point values (Velocity is always m/s)
        val avgVelocity = if (updatedPoints.isEmpty()) 0.0 else {
            updatedPoints.sumOf { it.velocity } / updatedPoints.size
        }
        val flowMetric = avgVelocity * widthMetric * depthMetric

        // Prepare the updated segment object for database (Metric)
        val finalizedSegment = segment.copy(
            segmentWidth = widthMetric,
            depth = depthMetric,
            averageVelocity = avgVelocity,
            segmentFlow = flowMetric
        )

        // Persist changes
        repository.updateSegment(finalizedSegment)
        
        // Replace points
        repository.deleteVelocityPoints(segment.id)
        updatedPoints.forEach { point ->
            // Ensure point height is also stored in meters
            val heightMetric = point.measureHeight?.let { 
                if (isHydrometric) it / 100.0 else it 
            }
            repository.insertVelocityPoint(point.copy(
                segmentId = segment.id,
                measureHeight = heightMetric
            ))
        }
    }
}
