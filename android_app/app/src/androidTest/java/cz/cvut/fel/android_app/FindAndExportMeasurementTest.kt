package cz.cvut.fel.android_app

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import cz.cvut.fel.android_app.data.AppDatabase
import cz.cvut.fel.android_app.domain.CalculateStreamSegmentUseCase
import cz.cvut.fel.android_app.domain.CompleteStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.CompleteStreamSegmentUseCase
import cz.cvut.fel.android_app.domain.ExportStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.GetStreamMeasurementSummaryUseCase
import cz.cvut.fel.android_app.domain.SearchMeasurementsUseCase
import cz.cvut.fel.android_app.domain.StartStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.helpers.TestDb
import cz.cvut.fel.android_app.helpers.capturedPoint
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FindAndExportMeasurementTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: StreamMeasurementRepository

    private lateinit var startMeasurement: StartStreamMeasurementUseCase
    private lateinit var completeSegment: CompleteStreamSegmentUseCase
    private lateinit var completeMeasurement: CompleteStreamMeasurementUseCase
    private lateinit var searchMeasurements: SearchMeasurementsUseCase
    private lateinit var exportMeasurement: ExportStreamMeasurementUseCase

    @Before
    fun setUp() {
        db = TestDb.build(ApplicationProvider.getApplicationContext())
        repository = TestDb.repository(db)

        val getSummary = GetStreamMeasurementSummaryUseCase(repository)
        startMeasurement = StartStreamMeasurementUseCase(repository)
        completeSegment = CompleteStreamSegmentUseCase(repository, CalculateStreamSegmentUseCase())
        completeMeasurement = CompleteStreamMeasurementUseCase(repository, getSummary)
        searchMeasurements = SearchMeasurementsUseCase(repository)
        exportMeasurement = ExportStreamMeasurementUseCase(repository)
    }

    @After
    fun tearDown() = db.close()

    private suspend fun seedMeasurement(name: String, note: String? = null): Int {
        val id = startMeasurement().toInt()
        completeSegment(id, segmentWidth = 1.0, depth = 0.5, points = listOf(capturedPoint(1.0)))
        completeMeasurement(id, name = name, note = note)
        return id
    }

    // ── search ───────────────────────────────────────────────────────────────

    @Test
    fun search_byName_returnsMatchingMeasurements() {
        runTest {
            seedMeasurement("River Labe")
            seedMeasurement("River Vltava")
            seedMeasurement("Canal South")

            searchMeasurements(query = "River").test {
                val results = awaitItem()
                assertEquals(2, results.size)
                assertTrue(results.all { it.name.contains("River") })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun search_emptyQuery_returnsAll() {
        runTest {
            seedMeasurement("A")
            seedMeasurement("B")
            seedMeasurement("C")

            searchMeasurements(query = "").test {
                assertEquals(3, awaitItem().size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // ── export ───────────────────────────────────────────────────────────────

    @Test
    fun export_containsMeasurementName() {
        runTest {
            val id = seedMeasurement("Orlice pramen")
            val csv = exportMeasurement(id, MeasurementUnit.HYDROMETRIC)
            assertTrue(csv.contains("Orlice pramen"))
        }
    }

    @Test
    fun export_containsOperatorInfo() {
        runTest {
            val id = seedMeasurement("Test")
            val csv = exportMeasurement(id, MeasurementUnit.HYDROMETRIC, operatorName = "Jan Novák", contactEmail = "jan@example.com")
            assertTrue(csv.contains("Jan Novák"))
            assertTrue(csv.contains("jan@example.com"))
        }
    }

    @Test
    fun export_hydrometric_usesCorrectLabels() {
        runTest {
            val id = seedMeasurement("Test")
            val csv = exportMeasurement(id, MeasurementUnit.HYDROMETRIC)
            assertTrue(csv.contains("cm"))
            assertTrue(csv.contains("L/s"))
        }
    }

    @Test
    fun export_metric_usesCorrectLabels() {
        runTest {
            val id = seedMeasurement("Test")
            val csv = exportMeasurement(id, MeasurementUnit.METRIC)
            assertTrue(csv.contains("m3/s"))
        }
    }

    @Test
    fun export_containsSegmentData() {
        runTest {
            val id = startMeasurement().toInt()
            completeSegment(id, 1.0, 0.5, listOf(capturedPoint(2.0)))
            completeSegment(id, 2.0, 0.3, listOf(capturedPoint(1.0)))
            completeMeasurement(id, name = "Two Segments")

            val csv = exportMeasurement(id, MeasurementUnit.HYDROMETRIC)

            assertTrue(csv.contains("SEGMENT DATA"))
            assertTrue(csv.contains("RAW VELOCITY READINGS"))
            assertTrue(csv.contains("1,2.00,"))
            assertTrue(csv.contains("2,1.00,"))
        }
    }
}