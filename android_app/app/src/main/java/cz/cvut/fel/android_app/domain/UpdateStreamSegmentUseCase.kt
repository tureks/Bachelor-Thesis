package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.model.ValidationResult
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

/** Validates, recomputes discharge, replaces velocity points, and refreshes measurement totals. */
class UpdateStreamSegmentUseCase(
    private val repository: StreamMeasurementRepository,
    private val validator: ValidateSegmentInputUseCase,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase
) {
    /**
     * Validates, recalculates, and persists an edited segment and its velocity points.
     * Refreshes the parent measurement totals.
     * @param segment updated segment; [StreamSegment.segmentWidth] and [StreamSegment.depth] in meters
     * @param updatedPoints replacement velocity points for the segment
     * @return [ValidationResult.Success] or [ValidationResult.Error] with a human-readable message
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
        repository.replaceVelocityPoints(segment.id, updatedPoints.map { it.copy(segmentId = segment.id) })

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
