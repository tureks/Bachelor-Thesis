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
    fun invoke_allPointsSelected_computesCorrectFlow() {
        val points = listOf(capturedPoint(1.0), capturedPoint(3.0))
        val result = useCase(segmentWidth = 2.0, depth = 0.5, points = points, selectedIndices = setOf(0, 1))

        assertEquals(2.0, result.averageVelocity, 1e-10)
        assertEquals(2.0, result.segmentFlow, 1e-10) // 2.0 * 2.0 * 0.5
    }

    @Test
    fun invoke_subsetSelected_usesOnlySelectedPoints() {
        val points = listOf(capturedPoint(1.0), capturedPoint(3.0), capturedPoint(5.0))
        val result = useCase(segmentWidth = 1.0, depth = 1.0, points = points, selectedIndices = setOf(0, 2))

        assertEquals(3.0, result.averageVelocity, 1e-10) // (1+5)/2
        assertEquals(2, result.selectedPoints.size)
    }

    @Test
    fun invoke_singlePointSelected_velocityEqualsPoint() {
        val points = listOf(capturedPoint(2.5))
        val result = useCase(segmentWidth = 1.0, depth = 1.0, points = points, selectedIndices = setOf(0))

        assertEquals(2.5, result.averageVelocity, 1e-10)
    }

    @Test
    fun invoke_noSelectedIndices_returnsZeroFlow() {
        val points = listOf(capturedPoint(2.0), capturedPoint(3.0))
        val result = useCase(segmentWidth = 1.0, depth = 1.0, points = points, selectedIndices = emptySet())

        assertEquals(0.0, result.averageVelocity, 0.0)
        assertEquals(0.0, result.segmentFlow, 0.0)
        assertEquals(0, result.selectedPoints.size)
    }

    @Test
    fun invoke_emptyPointsList_returnsZeroFlow() {
        val result = useCase(segmentWidth = 1.0, depth = 1.0, points = emptyList(), selectedIndices = emptySet())

        assertEquals(0.0, result.segmentFlow, 0.0)
    }

}