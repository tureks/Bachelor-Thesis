package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.ValidationResult

class ValidateSegmentInputUseCase {

    /**
     * Validates hydrological segment input parameters.
     * @param width Segment width in meters.
     * @param depth Segment depth in meters.
     * @param velocity Average velocity in m/s.
     * @param height Measurement height as a ratio of depth (0.0–1.0)
     * @return [ValidationResult.Success] or [ValidationResult.Error] with a human-readable message.
     */
    operator fun invoke(
        width: Double,
        depth: Double,
        velocity: Double,
        height: Double? = null
    ): ValidationResult {
        if (width <= 0) {
            return ValidationResult.Error("Width must be greater than zero.")
        }
        if (depth <= 0) {
            return ValidationResult.Error("Depth must be greater than zero.")
        }
        if (velocity < 0) {
            return ValidationResult.Error("Velocity cannot be negative.")
        }
        if (velocity > 5.0) {
            return ValidationResult.Error("Velocity exceeds hardware limit (5.0 m/s).")
        }
        
        height?.let {
            if (it < 0.0 || it > 1.0) {
                return ValidationResult.Error("Relative height must be between 0.0 and 1.0 (0-100%).")
            }
        }

        return ValidationResult.Success
    }
}
