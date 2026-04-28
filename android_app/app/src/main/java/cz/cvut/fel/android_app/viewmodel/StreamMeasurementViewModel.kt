package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.*
import cz.cvut.fel.android_app.domain.model.*
import cz.cvut.fel.android_app.domain.repository.BleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StreamMeasurementUiState(
    val measurement: StreamMeasurement? = null,
    val segments: List<StreamSegment> = emptyList(),
    val totals: StreamMeasurementTotals? = null,
    val currentVelocity: Double = 0.0,
    val isCapturing: Boolean = false,
    val captureProgress: Float = 0f,
    val capturedPoints: List<CapturedVelocityPoint> = emptyList(),
    val currentLocation: Location? = null,
    val connectionState: BleConnectionState = BleConnectionState.Idle,
    val batteryLevel: Int = 0,
    val error: String? = null
)

class StreamMeasurementViewModel(
    private val startMeasurementUseCase: StartStreamMeasurementUseCase,
    private val captureSegmentUseCase: CaptureStreamSegmentUseCase,
    private val completeSegmentUseCase: CompleteStreamSegmentUseCase,
    private val completeMeasurementUseCase: CompleteStreamMeasurementUseCase,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase,
    private val observeBleStateUseCase: ObserveBleConnectionStateUseCase,
    private val observeBatteryUseCase: ObserveBatteryLevelUseCase,
    private val observeLocationUseCase: ObserveLocationUseCase,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreamMeasurementUiState())
    val uiState: StateFlow<StreamMeasurementUiState> = _uiState.asStateFlow()

    private var captureJob: Job? = null

    init {
        // Observe Hardware & Location
        observeBleStateUseCase().onEach { state ->
            _uiState.update { it.copy(connectionState = state) }
        }.launchIn(viewModelScope)

        observeBatteryUseCase().onEach { level ->
            _uiState.update { it.copy(batteryLevel = level) }
        }.launchIn(viewModelScope)

        observeLocationUseCase().onEach { location ->
            _uiState.update { it.copy(currentLocation = location) }
        }.launchIn(viewModelScope)

        bleRepository.velocityReadings.onEach { velocity ->
            _uiState.update { it.copy(currentVelocity = velocity) }
        }.launchIn(viewModelScope)
    }

    /**
     * Starts a new measurement draft.
     */
    fun startNewMeasurement() {
        viewModelScope.launch {
            val id = startMeasurementUseCase()
            refreshData(id.toInt())
        }
    }

    /**
     * Captures velocity over a 10s window. 
     */
    fun startCapture() {
        if (_uiState.value.isCapturing) return
        
        _uiState.update { it.copy(isCapturing = true, captureProgress = 0f, capturedPoints = emptyList()) }
        
        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            val readings = mutableListOf<Double>()
            
            // Raw readings are needed to build CapturedVelocityPoints
            val rawCollector = bleRepository.velocityReadings.onEach { readings.add(it) }.launchIn(this)

            captureSegmentUseCase(bleRepository.velocityReadings).collect { stats ->
                // Approximate progress (100 samples / 10s window)
                val progress = readings.size / 100f 
                _uiState.update { it.copy(captureProgress = progress.coerceAtMost(1f)) }

                if (progress >= 1f) {
                    rawCollector.cancel()
                    val points = readings.map { CapturedVelocityPoint(it, null) }
                    _uiState.update { it.copy(isCapturing = false, capturedPoints = points) }
                    cancel() 
                }
            }
        }
    }

    /**
     * Completes a segment and persists it.
     */
    fun completeSegment(width: Double, depth: Double) {
        val measurementId = _uiState.value.measurement?.id ?: return
        val points = _uiState.value.capturedPoints
        
        if (points.isEmpty()) {
            _uiState.update { it.copy(error = "No velocity data captured.") }
            return
        }

        viewModelScope.launch {
            try {
                completeSegmentUseCase(
                    measurementId = measurementId,
                    segmentWidth = width,
                    depth = depth,
                    points = points,
                    selectedIndices = points.indices.toSet()
                )
                refreshData(measurementId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * Finalizes the entire measurement session.
     */
    fun finalizeMeasurement(name: String, notes: String) {
        val measurement = _uiState.value.measurement ?: return
        val location = _uiState.value.currentLocation
        
        viewModelScope.launch {
            try {
                completeMeasurementUseCase(
                    measurementId = measurement.id,
                    name = name,
                    note = notes,
                    gpsLat = location?.latitude,
                    gpsLong = location?.longitude
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private suspend fun refreshData(measurementId: Int) {
        val totals = getSummaryUseCase(measurementId)
        _uiState.update { it.copy(
            measurement = _uiState.value.measurement?.copy(id = measurementId),
            totals = totals
        ) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                val summaryUseCase = GetStreamMeasurementSummaryUseCase(app.measurementRepository, app.userRepository)
                
                StreamMeasurementViewModel(
                    startMeasurementUseCase = StartStreamMeasurementUseCase(app.measurementRepository),
                    captureSegmentUseCase = CaptureStreamSegmentUseCase(),
                    completeSegmentUseCase = CompleteStreamSegmentUseCase(
                        app.measurementRepository, 
                        app.userRepository,
                        CalculateStreamSegmentUseCase()
                    ),
                    completeMeasurementUseCase = CompleteStreamMeasurementUseCase(app.measurementRepository, summaryUseCase),
                    getSummaryUseCase = summaryUseCase,
                    observeBleStateUseCase = ObserveBleConnectionStateUseCase(app.bleRepository),
                    observeBatteryUseCase = ObserveBatteryLevelUseCase(app.bleRepository),
                    observeLocationUseCase = ObserveLocationUseCase(app.locationRepository),
                    bleRepository = app.bleRepository
                )
            }
        }
    }
}
