package cz.cvut.fel.android_app.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

class CaptureStreamSegmentUseCase(val windowSize: Int = DEFAULT_WINDOW_SIZE) {

    operator fun invoke(velocityReadings: Flow<Double>): Flow<Double> =
        velocityReadings
            .scan(emptyList<Double>()) { window, reading ->
                (window + reading).takeLast(windowSize)
            }
            .filter { it.isNotEmpty() }
            .map { it.average() }

    companion object {
        const val DEFAULT_WINDOW_SIZE = 50
    }
}