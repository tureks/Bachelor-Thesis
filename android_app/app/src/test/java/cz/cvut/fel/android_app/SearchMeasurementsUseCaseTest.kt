package cz.cvut.fel.android_app

import app.cash.turbine.test
import cz.cvut.fel.android_app.domain.SearchMeasurementsUseCase
import cz.cvut.fel.android_app.fixtures.FakeStreamMeasurementRepository
import cz.cvut.fel.android_app.fixtures.measurement
import cz.cvut.fel.android_app.domain.model.StreamMeasurementStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchMeasurementsUseCaseTest {

    private lateinit var repository: FakeStreamMeasurementRepository
    private lateinit var useCase: SearchMeasurementsUseCase

    @Before
    fun setUp() {
        repository = FakeStreamMeasurementRepository()
        useCase = SearchMeasurementsUseCase(repository)
    }

    @Test
    fun emptyQuery_returnsAllCompletedSortedDescending_test() {
        runTest {
            repository.seed(
                measurement(id = 1, name = "Alpha", measureTimestamp = 1000L),
                measurement(id = 2, name = "Beta",  measureTimestamp = 3000L),
                measurement(id = 3, name = "Gamma", measureTimestamp = 2000L),
            )

            useCase(query = "").test {
                val result = awaitItem()
                assertEquals(3, result.size)
                assertEquals(3000L, result[0].measureTimestamp)
                assertEquals(2000L, result[1].measureTimestamp)
                assertEquals(1000L, result[2].measureTimestamp)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun queryMatchesName_returnsFilteredResults_test() {
        runTest {
            repository.seed(
                measurement(id = 1, name = "River Labe"),
                measurement(id = 2, name = "River Vltava"),
                measurement(id = 3, name = "Canal"),
            )

            useCase(query = "river").test {
                val result = awaitItem()
                assertEquals(2, result.size)
                assertTrue(result.all { it.name.contains("River", ignoreCase = true) })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun queryMatchesNote_returnsResult_test() {
        runTest {
            repository.seed(
                measurement(id = 1, name = "Station A", note = "spring flood measurement"),
                measurement(id = 2, name = "Station B", note = null),
            )

            useCase(query = "flood").test {
                val result = awaitItem()
                assertEquals(1, result.size)
                assertEquals(1, result[0].id)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun queryIsCaseInsensitive_test() {
        runTest {
            repository.seed(measurement(id = 1, name = "MORAVA"))

            useCase(query = "morava").test {
                val result = awaitItem()
                assertEquals(1, result.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun fromTimestamp_filtersOlderMeasurements_test() {
        runTest {
            repository.seed(
                measurement(id = 1, measureTimestamp = 500L),
                measurement(id = 2, measureTimestamp = 1500L),
                measurement(id = 3, measureTimestamp = 2500L),
            )

            useCase(fromTimestamp = 1000L).test {
                val result = awaitItem()
                assertEquals(2, result.size)
                assertTrue(result.all { it.measureTimestamp >= 1000L })
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun fromTimestamp_sortsByTimestampAscending_test() {
        runTest {
            repository.seed(
                measurement(id = 1, measureTimestamp = 2000L),
                measurement(id = 2, measureTimestamp = 1000L),
            )

            useCase(fromTimestamp = 0L).test {
                val result = awaitItem()
                assertEquals(1000L, result[0].measureTimestamp)
                assertEquals(2000L, result[1].measureTimestamp)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun noMatchingQuery_returnsEmpty_test() {
        runTest {
            repository.seed(measurement(id = 1, name = "Station A"))

            useCase(query = "xyz").test {
                val result = awaitItem()
                assertTrue(result.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun emptyRepository_returnsEmpty_test() {
        runTest {
            useCase(query = "").test {
                assertTrue(awaitItem().isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}