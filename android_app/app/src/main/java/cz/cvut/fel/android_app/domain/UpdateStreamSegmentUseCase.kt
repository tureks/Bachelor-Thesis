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
     * All values (segment.segmentWidth, segment.depth) must be in SI units (meters).
     */
    suspend operator fun invoke(
        segment: StreamSegment,
        updatedPoints: List<VelocityPoint>
    ): ValidationResult {
        val width = segment.segmentWidth
        val depth = segment.depth

        val avgVelocity = if (updatedPoints.isEmpty()) 0.0 else {
            updatedPoints.sumOf { it.velocity } / updatedPoints.size
        }

        val validation = validator(width = width, depth = depth, velocity = avgVelocity)
        if (validation is ValidationResult.Error) return validation

        val flow = avgVelocity * width * depth

        val finalizedSegment = segment.copy(
            segmentWidth = width,
            depth = depth,
            averageVelocity = avgVelocity,
            segmentFlow = flow
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
