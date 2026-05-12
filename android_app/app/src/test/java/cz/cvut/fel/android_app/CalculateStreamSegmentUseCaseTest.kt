package cz.cvut.fel.android_app

import cz.cvut.fel.android_app.domain.CalculateStreamSegmentUseCase
import cz.cvut.fel.android_app.fixtures.capturedPoint
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CalculateStreamSegmentUseCaseTest {

    private lateinit var useCase: CalculateStreamSegmentUseCase

    @Before
    fun setUp() {
        useCase = CalculateStreamSegmentUseCase()
    }

    @Test
    fun invoke_multiplePoints_computesAverageVelocityAndFlow() {
        val points = listOf(capturedPoint(1.0), capturedPoint(3.0))
        val result = useCase(segmentWidth = 2.0, depth = 0.5, points = points)

        assertEquals(2.0, result.averageVelocity, 1e-10)
        assertEquals(2.0, result.segmentFlow, 1e-10) // 2.0 * 2.0 * 0.5
    }

    @Test
    fun invoke_singlePoint_velocityEqualsPoint() {
        val points = listOf(capturedPoint(2.5))
        val result = useCase(segmentWidth = 1.0, depth = 1.0, points = points)

        assertEquals(2.5, result.averageVelocity, 1e-10)
        assertEquals(2.5, result.segmentFlow, 1e-10)
    }

    @Test
    fun invoke_emptyPoints_returnsZeroFlow() {
        val result = useCase(segmentWidth = 1.0, depth = 1.0, points = emptyList())

        assertEquals(0.0, result.averageVelocity, 0.0)
        assertEquals(0.0, result.segmentFlow, 0.0)
    }

    @Test
    fun invoke_dimensionsScaleFlow() {
        val points = listOf(capturedPoint(2.0))
        val result = useCase(segmentWidth = 3.0, depth = 0.5, points = points)

        assertEquals(3.0, result.segmentFlow, 1e-10) // 2.0 * 3.0 * 0.5
    }
}