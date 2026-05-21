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
    fun validInputs_returnsSuccess_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun validInputsWithHeight_returnsSuccess_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0, height = 0.6)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun velocityAtMaxBoundary_returnsSuccess_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 5.0)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun velocityZero_returnsSuccess_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 0.0)
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun heightAtBoundaries_returnsSuccess_test() {
        assertEquals(ValidationResult.Success, useCase(1.0, 0.5, 1.0, height = 0.0))
        assertEquals(ValidationResult.Success, useCase(1.0, 0.5, 1.0, height = 1.0))
    }

    @Test
    fun widthZero_returnsError_test() {
        val result = useCase(width = 0.0, depth = 0.5, velocity = 1.0)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun widthNegative_returnsError_test() {
        val result = useCase(width = -1.0, depth = 0.5, velocity = 1.0)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun depthZero_returnsError_test() {
        val result = useCase(width = 1.0, depth = 0.0, velocity = 1.0)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun depthNegative_returnsError_test() {
        val result = useCase(width = 1.0, depth = -0.1, velocity = 1.0)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun velocityNegative_returnsError_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = -0.001)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun velocityAboveLimit_returnsError_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 5.001)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun heightBelowZero_returnsError_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0, height = -0.01)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun heightAboveOne_returnsError_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0, height = 1.01)
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun heightNull_ignored_test() {
        val result = useCase(width = 1.0, depth = 0.5, velocity = 1.0, height = null)
        assertEquals(ValidationResult.Success, result)
    }
}