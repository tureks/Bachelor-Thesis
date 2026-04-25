package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.VelocityStats
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.scan

class CaptureStreamSegmentUseCase(
    windowDurationMs: Long = DEFAULT_WINDOW_DURATION_MS
) {
    private val windowSize = (windowDurationMs / SAMPLING_INTERVAL_MS).toInt()

    @OptIn(FlowPreview::class)
    operator fun invoke(velocityReadings: Flow<Double>): Flow<VelocityStats> =
        velocityReadings
            .scan(emptyList<Double>()) { window, reading ->
                (window + reading).takeLast(windowSize)
            }
            .filter { it.isNotEmpty() }
            .sample(UI_UPDATE_INTERVAL_MS)
            .map { window ->
                VelocityStats(
                    average = window.average(),
                    min = window.minOrNull() ?: 0.0,
                    max = window.maxOrNull() ?: 0.0
                )
            }

    companion object {
        const val SAMPLING_INTERVAL_MS = 100L
        const val DEFAULT_WINDOW_DURATION_MS = 10000L // 10 seconds
        const val UI_UPDATE_INTERVAL_MS = 1200L
    }
}
