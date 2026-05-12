package cz.cvut.fel.android_app

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.cvut.fel.android_app.data.AppDatabase
import cz.cvut.fel.android_app.domain.CalculateStreamSegmentUseCase
import cz.cvut.fel.android_app.domain.CompleteStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.CompleteStreamSegmentUseCase
import cz.cvut.fel.android_app.domain.GetStreamMeasurementSummaryUseCase
import cz.cvut.fel.android_app.domain.StartStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.helpers.TestDb
import cz.cvut.fel.android_app.helpers.capturedPoint
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateMeasurementWithTwoSegmentsTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: StreamMeasurementRepository

    private lateinit var startMeasurement: StartStreamMeasurementUseCase
    private lateinit var completeSegment: CompleteStreamSegmentUseCase
    private lateinit var completeMeasurement: CompleteStreamMeasurementUseCase
    private lateinit var getSummary: GetStreamMeasurementSummaryUseCase

    @Before
    fun setUp() {
        db = TestDb.build(ApplicationProvider.getApplicationContext())
        repository = TestDb.repository(db)

        getSummary = GetStreamMeasurementSummaryUseCase(repository)
        startMeasurement = StartStreamMeasurementUseCase(repository)
        completeSegment = CompleteStreamSegmentUseCase(repository, CalculateStreamSegmentUseCase())
        completeMeasurement = CompleteStreamMeasurementUseCase(repository, getSummary)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun createMeasurementWithTwoSegments_savesCorrectTotals() {
        runTest {
            val measurementId = startMeasurement().toInt()
            val draft = repository.getById(measurementId)
            assertNotNull(draft)
            assertEquals(StreamMeasurementStatus.DRAFT, draft!!.status)
            assertEquals(0, repository.getSegments(measurementId).size)

            // segment 1: width=1.0m, depth=0.5m, velocity=2.0 m/s → flow=1.0 m³/s
            val seg1Points = listOf(capturedPoint(2.0), capturedPoint(2.0))
            completeSegment(measurementId, segmentWidth = 1.0, depth = 0.5, points = seg1Points)
            val segsAfter1 = repository.getSegments(measurementId)
            assertEquals(1, segsAfter1.size)
            assertEquals(1.0, segsAfter1[0].segmentFlow, 1e-10)
            assertEquals(2, repository.getVelocityPoints(segsAfter1[0].id).size)

            // segment 2: width=2.0m, depth=0.3m, velocity=1.0 m/s → flow=0.6 m³/s
            val seg2Points = listOf(capturedPoint(1.0))
            completeSegment(measurementId, segmentWidth = 2.0, depth = 0.3, points = seg2Points)
            val segsAfter2 = repository.getSegments(measurementId)
            assertEquals(2, segsAfter2.size)
            assertEquals(0.6, segsAfter2[1].segmentFlow, 1e-10)
            assertEquals(1, repository.getVelocityPoints(segsAfter2[1].id).size)

            completeMeasurement(measurementId, name = "Upa", note = "spring test")

            val saved = repository.getById(measurementId)
            assertNotNull(saved)
            assertEquals(StreamMeasurementStatus.COMPLETE, saved!!.status)
            assertEquals("Upa", saved.name)
            assertEquals(3.0, saved.totalWidth!!, 1e-10)   // 1.0 + 2.0
            assertEquals(0.5, saved.maxDepth!!, 1e-10)     // max(0.5, 0.3)
            assertEquals(1.6, saved.totalFlow!!, 1e-10)    // 1.0 + 0.6
        }
    }

    @Test
    fun createMeasurementWithTwoSegments_segmentsNumberedInOrder() {
        runTest {
            val measurementId = startMeasurement().toInt()
            assertEquals(0, repository.getSegments(measurementId).size)

            completeSegment(measurementId, 1.0, 0.5, listOf(capturedPoint(1.0)))
            val segsAfter1 = repository.getSegments(measurementId)
            assertEquals(1, segsAfter1.size)
            assertEquals(1, segsAfter1[0].segmentNumber)

            completeSegment(measurementId, 1.0, 0.5, listOf(capturedPoint(1.0)))
            val segments = repository.getSegments(measurementId)
            assertEquals(2, segments.size)
            assertEquals(1, segments[0].segmentNumber)
            assertEquals(2, segments[1].segmentNumber)
        }
    }

    @Test
    fun createMeasurementWithTwoSegments_velocityPointsPersistedWithCorrectSegmentId() {
        runTest {
            val measurementId = startMeasurement().toInt()
            assertEquals(0, repository.getSegments(measurementId).size)

            completeSegment(measurementId, 1.0, 0.5, listOf(capturedPoint(1.0), capturedPoint(2.0)))
            val segsAfter1 = repository.getSegments(measurementId)
            assertEquals(1, segsAfter1.size)
            assertEquals(2, repository.getVelocityPoints(segsAfter1[0].id).size)

            completeSegment(measurementId, 1.0, 0.5, listOf(capturedPoint(3.0)))
            val segments = repository.getSegments(measurementId)
            val pointsSeg1 = repository.getVelocityPoints(segments[0].id)
            val pointsSeg2 = repository.getVelocityPoints(segments[1].id)

            assertEquals(2, pointsSeg1.size)
            assertEquals(1, pointsSeg2.size)
            assertEquals(3.0, pointsSeg2[0].velocity, 1e-10)
        }
    }
}