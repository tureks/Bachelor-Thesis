package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.model.ValidationResult
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.first

class UpdateStreamSegmentUseCase(
    private val repository: StreamMeasurementRepository,
    private val userRepository: UserRepository,
    private val validator: ValidateSegmentInputUseCase,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase
) {
    /**
     * Updates an existing segment and its points.
     * Inputs (segment.segmentWidth, segment.depth) are expected in user's preferred unit (cm/m).
     */
    suspend operator fun invoke(
        segment: StreamSegment,
        updatedPoints: List<VelocityPoint>
    ): ValidationResult {
        val user = userRepository.user.first()
        val isHydrometric = user?.preferredUnit == MeasurementUnit.HYDROMETRIC

        // Convert UI inputs to Metric (m)
        val widthMetric = if (isHydrometric) segment.segmentWidth / 100.0 else segment.segmentWidth
        val depthMetric = if (isHydrometric) segment.depth / 100.0 else segment.depth

        val avgVelocity = if (updatedPoints.isEmpty()) 0.0 else {
            updatedPoints.sumOf { it.velocity } / updatedPoints.size
        }

        // --- VALIDATION CHECK ---
        val validation = validator(
            width = widthMetric,
            depth = depthMetric,
            velocity = avgVelocity
        )
        if (validation is ValidationResult.Error) return validation

        // Recalculate flow
        val flowMetric = avgVelocity * widthMetric * depthMetric

        // Prepare finalized segment (Metric)
        val finalizedSegment = segment.copy(
            segmentWidth = widthMetric,
            depth = depthMetric,
            averageVelocity = avgVelocity,
            segmentFlow = flowMetric
        )

        repository.updateSegment(finalizedSegment)
        
        repository.deleteVelocityPoints(segment.id)
        updatedPoints.forEach { point ->
            repository.insertVelocityPoint(point.copy(segmentId = segment.id))
        }

        // Update the parent measurement summary
        val measurement = repository.getById(segment.measurementId)
        if (measurement != null) {
            val summary = getSummaryUseCase(segment.measurementId)
            repository.update(measurement.copy(
                totalWidth = summary.totalWidth,
                maxDepth = summary.maxDepth,
                totalFlow = summary.totalFlow
            ))
        }

        return ValidationResult.Success
    }
}
