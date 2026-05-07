package cz.cvut.fel.android_app

import cz.cvut.fel.android_app.domain.ValidateSegmentInputUseCase
import cz.cvut.fel.android_app.domain.model.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateSegmentInputUseCaseTest {

    private lateinit var useCase: ValidateSegmentInputUseCase

    @Before
    fun setUp() {
        useCase = ValidateSegmentInputUseCase()
    }

    @Test
    fun invoke_validInputs_returnsSuccess() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun invoke_validInputsWithHeight_returnsSuccess() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0, height = 0.6)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun invoke_velocityAtMaxBoundary_returnsSuccess() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 5.0)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun invoke_velocityZero_returnsSuccess() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 0.0)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun invoke_heightAtBoundaries_returnsSuccess() {
        assertEquals(ValidationResult.Success, useCase(1.0, 0.5, 1.0, height = 0.0))
        assertEquals(ValidationResult.Success, useCase(1.0, 0.5, 1.0, height = 1.0))
    }

    @Test
    fun invoke_widthZero_returnsError() {
        val result = useCase(width = 0.0, depth = 0.5, velocity = 1.0)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun invoke_widthNegative_returnsError() {
        val result = useCase(width = -1.0, depth = 0.5, velocity = 1.0)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun invoke_depthZero_returnsError() {
        val result = useCase(width = 1.0, depth = 0.0, velocity = 1.0)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun invoke_depthNegative_returnsError() {
        val result = useCase(width = 1.0, depth = -0.1, velocity = 1.0)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun invoke_velocityNegative_returnsError() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = -0.001)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun invoke_velocityAboveLimit_returnsError() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 5.001)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun invoke_heightBelowZero_returnsError() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0, height = -0.01)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun invoke_heightAboveOne_returnsError() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0, height = 1.01)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun invoke_heightNull_ignored() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0, height = null)
        assertEquals(ValidationResult.Success, result)
    }
}