package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.repository.BleRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

const val VELOCITY_MAX = 5.0

data class VelocityReading(val velocity: Double, val timestamp: Long)

data class BleUiState(
    val connectionState: BleConnectionState = BleConnectionState.Idle,
    val batteryLevel: Int = 0,
    val probeConnected: Boolean = false,
    val currentVelocity: Double = 0.0,
    val timeWindow: Int = 10,
    val windowAverage: Double = 0.0,
    val windowMin: Double = 0.0,
    val windowMax: Double = 0.0,
    val recentReadings: List<VelocityReading> = emptyList(),
    val velocityOverLimit: Boolean = false
)

class BleViewModel(private val bleRepository: BleRepository) : ViewModel() {

    private val _timeWindow = MutableStateFlow(10)

    private data class HardwareBase(
        val velocity: Double,
        val connectionState: BleConnectionState,
        val batteryLevel: Int,
        val probeConnected: Boolean
    )

    private val hardwareBase: Flow<HardwareBase> = combine(
        bleRepository.velocityReadings.onStart { emit(0.0) },
        bleRepository.connectionState,
        bleRepository.batteryLevel,
        bleRepository.probeConnected
    ) { v, c, b, p -> HardwareBase(v, c, b, p) }

    private val recentReadingsFlow: Flow<List<VelocityReading>> = bleRepository.velocityReadings
        .scan(emptyList<VelocityReading>()) { acc, velocity ->
            val now = System.currentTimeMillis()
            val windowStart = now - (_timeWindow.value * 1000L)
            (acc + VelocityReading(velocity, now)).filter { it.timestamp >= windowStart }
        }
        .onStart { emit(emptyList()) }

    @OptIn(FlowPreview::class)
    private val windowAverageFlow: Flow<Double> = combine(recentReadingsFlow, _timeWindow) { readings, window ->
        val now = System.currentTimeMillis()
        val windowStart = now - window * 1000L
        val windowed = readings.filter { it.timestamp >= windowStart }
        val startIdx = windowed.indexOfFirst { it.velocity > 0.0 }
        if (startIdx == -1) 0.0 else windowed.drop(startIdx).map { it.velocity }.average()
    }
        .sample(1200L)
        .distinctUntilChanged()
        .onStart { emit(0.0) }

    val uiState: StateFlow<BleUiState> = combine(
        hardwareBase,
        recentReadingsFlow,
        _timeWindow,
        windowAverageFlow
    ) { hardware, readings, window, avg ->
        val windowStart = System.currentTimeMillis() - window * 1000L
        val liveReadings = readings.filter { it.timestamp >= windowStart }
        val startIdx = liveReadings.indexOfFirst { it.velocity > 0.0 }
        val averagingReadings = if (startIdx == -1) emptyList() else liveReadings.drop(startIdx)
        BleUiState(
            connectionState = hardware.connectionState,
            batteryLevel = hardware.batteryLevel,
            probeConnected = hardware.probeConnected,
            currentVelocity = hardware.velocity,
            timeWindow = window,
            windowAverage = avg,
            windowMin = averagingReadings.minOfOrNull { it.velocity } ?: 0.0,
            windowMax = averagingReadings.maxOfOrNull { it.velocity } ?: 0.0,
            recentReadings = liveReadings,
            velocityOverLimit = hardware.velocity > VELOCITY_MAX
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BleUiState())

    fun setTimeWindow(seconds: Int) {
        _timeWindow.value = seconds
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                BleViewModel(bleRepository = (this[APPLICATION_KEY] as App).bleRepository)
            }
        }
    }
}