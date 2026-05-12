package cz.cvut.fel.android_app

import cz.cvut.fel.android_app.domain.GetStreamMeasurementSummaryUseCase
import cz.cvut.fel.android_app.fixtures.FakeStreamMeasurementRepository
import cz.cvut.fel.android_app.fixtures.segment
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetStreamMeasurementSummaryUseCaseTest {

    private lateinit var repository: FakeStreamMeasurementRepository
    private lateinit var useCase: GetStreamMeasurementSummaryUseCase

    @Before
    fun setUp() {
        repository = FakeStreamMeasurementRepository()
        useCase = GetStreamMeasurementSummaryUseCase(repository)
    }

    @Test
    fun invoke_noSegments_returnsAllZeros() {
        runTest {
            val totals = useCase(measurementId = 1)

            assertEquals(0.0, totals.totalWidth, 0.0)
            assertEquals(0.0, totals.maxDepth, 0.0)
            assertEquals(0.0, totals.totalFlow, 0.0)
        }
    }

    @Test
    fun invoke_twoSegments_sumsTotalWidthAndFlow() {
        runTest {
            repository.seedSegments(
                segment(id = 1, measurementId = 1, segmentWidth = 1.0, depth = 0.5, segmentFlow = 0.5),
                segment(id = 2, measurementId = 1, segmentWidth = 2.0, depth = 0.3, segmentFlow = 0.6),
            )

            val totals = useCase(measurementId = 1)

            assertEquals(3.0, totals.totalWidth, 1e-10)
            assertEquals(0.5, totals.maxDepth, 1e-10)
            assertEquals(1.1, totals.totalFlow, 1e-10)
        }
    }

    @Test
    fun invoke_multipleSegments_picksMaxDepth() {
        runTest {
            repository.seedSegments(
                segment(id = 1, measurementId = 1, depth = 0.2),
                segment(id = 2, measurementId = 1, depth = 0.8),
                segment(id = 3, measurementId = 1, depth = 0.5),
            )

            val totals = useCase(measurementId = 1)

            assertEquals(0.8, totals.maxDepth, 1e-10)
        }
    }
}