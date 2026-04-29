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
import cz.cvut.fel.android_app.domain.repository.LocationRepository
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class VelocityReading(val velocity: Double, val timestamp: Long)
data class ManualVelocityPoint(val id: Long, val velocity: Double, val height: Double = 0.0)

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
    val timeWindow: Int = 10, // seconds
    val windowAverage: Double = 0.0,
    val recentReadings: List<VelocityReading> = emptyList(),
    val manualPoints: List<ManualVelocityPoint> = emptyList(),
    val preferredUnit: MeasurementUnit = MeasurementUnit.HYDROMETRIC,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class StreamMeasurementViewModel(
    private val startMeasurementUseCase: StartStreamMeasurementUseCase,
    private val captureSegmentUseCase: CaptureStreamSegmentUseCase,
    private val completeSegmentUseCase: CompleteStreamSegmentUseCase,
    private val completeMeasurementUseCase: CompleteStreamMeasurementUseCase,
    private val updateSegmentUseCase: UpdateStreamSegmentUseCase,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase,
    private val bleRepository: BleRepository,
    private val locationRepository: LocationRepository,
    private val measurementRepository: StreamMeasurementRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _timeWindow = MutableStateFlow(10)
    private val _manualPoints = MutableStateFlow<List<ManualVelocityPoint>>(emptyList())
    private val _captureState = MutableStateFlow(CaptureState())

    private data class CaptureState(
        val isCapturing: Boolean = false,
        val progress: Float = 0f,
        val points: List<CapturedVelocityPoint> = emptyList()
    )

    private data class MeasurementData(
        val draft: StreamMeasurement?,
        val segments: List<StreamSegment>,
        val totals: StreamMeasurementTotals?
    )

    private data class HardwareState(
        val velocity: Double,
        val connectionState: BleConnectionState,
        val batteryLevel: Int,
        val currentLocation: Location?
    )

    private val measurementDataFlow: Flow<MeasurementData> = 
        measurementRepository.getDraftFlow().flatMapLatest { draft ->
            if (draft != null) {
                measurementRepository.getSegmentsFlow(draft.id).map { segments ->
                    val totals = getSummaryUseCase(draft.id)
                    MeasurementData(draft, segments, totals)
                }
            } else {
                flowOf(MeasurementData(null, emptyList(), null))
            }
        }

    private val hardwareFlow: Flow<HardwareState> = combine(
        bleRepository.velocityReadings,
        bleRepository.connectionState,
        bleRepository.batteryLevel,
        locationRepository.observeLocation()
    ) { velocity, connection, battery, location ->
        HardwareState(velocity, connection, battery, location)
    }

    private val userUnitFlow: Flow<MeasurementUnit> = userRepository.user
        .map { it?.preferredUnit ?: MeasurementUnit.HYDROMETRIC }
        .distinctUntilChanged()

    private val recentReadingsFlow: Flow<List<VelocityReading>> = bleRepository.velocityReadings
        .scan(emptyList<VelocityReading>()) { accumulator, velocity ->
            val now = System.currentTimeMillis()
            val windowStart = now - (_timeWindow.value * 1000)
            (accumulator + VelocityReading(velocity, now))
                .filter { it.timestamp >= windowStart }
        }

    val uiState: StateFlow<StreamMeasurementUiState> = combine(
        measurementDataFlow,
        hardwareFlow,
        _captureState,
        recentReadingsFlow,
        _timeWindow,
        _manualPoints,
        userUnitFlow,
        _error
    ) { args ->
        val data = args[0] as MeasurementData
        val hardware = args[1] as HardwareState
        val capture = args[2] as CaptureState
        val readings = args[3] as List<VelocityReading>
        val window = args[4] as Int
        val points = args[5] as List<ManualVelocityPoint>
        val unit = args[6] as MeasurementUnit
        val error = args[7] as String?

        StreamMeasurementUiState(
            measurement = data.draft,
            segments = data.segments,
            totals = data.totals,
            currentVelocity = hardware.velocity,
            connectionState = hardware.connectionState,
            batteryLevel = hardware.batteryLevel,
            currentLocation = hardware.currentLocation,
            isCapturing = capture.isCapturing,
            captureProgress = capture.progress,
            capturedPoints = capture.points,
            timeWindow = window,
            recentReadings = readings,
            windowAverage = if (readings.isEmpty()) 0.0 else readings.map { it.velocity }.average(),
            manualPoints = points,
            preferredUnit = unit,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreamMeasurementUiState())

    fun setTimeWindow(seconds: Int) {
        _timeWindow.value = seconds
    }

    fun addManualPoint() {
        val avg = uiState.value.windowAverage
        _manualPoints.update { it + ManualVelocityPoint(System.currentTimeMillis(), avg) }
    }

    fun updateManualPointHeight(id: Long, height: Double) {
        _manualPoints.update { list ->
            list.map { if (it.id == id) it.copy(height = height) else it }
        }
    }

    fun deleteManualPoint(id: Long) {
        _manualPoints.update { it.filterNot { p -> p.id == id } }
    }

    private var captureJob: Job? = null

    fun startNewMeasurement() {
        viewModelScope.launch {
            try {
                startMeasurementUseCase()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun startCapture() {
        if (_captureState.value.isCapturing) return
        _captureState.update { it.copy(isCapturing = true, progress = 0f, points = emptyList()) }
        
        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            val readings = mutableListOf<Double>()
            val rawCollector = bleRepository.velocityReadings.onEach { readings.add(it) }.launchIn(this)

            captureSegmentUseCase(bleRepository.velocityReadings).collect { stats ->
                val progress = readings.size / 100f 
                _captureState.update { it.copy(progress = progress.coerceAtMost(1f)) }

                if (progress >= 1f) {
                    rawCollector.cancel()
                    val points = readings.map { CapturedVelocityPoint(it, null) }
                    _captureState.update { it.copy(isCapturing = false, points = points) }
                    this.cancel() 
                }
            }
        }
    }

    fun completeSegment(width: Double, depth: Double, selectedPointIds: Set<Long>) {
        val measurementId = uiState.value.measurement?.id ?: return
        val points = uiState.value.manualPoints
        
        if (points.isEmpty()) {
            _error.value = "No velocity data captured."
            return
        }

        val filteredPoints = points.filter { selectedPointIds.contains(it.id) }
        if (filteredPoints.isEmpty()) {
            _error.value = "Please select at least one velocity point."
            return
        }

        viewModelScope.launch {
            try {
                completeSegmentUseCase(
                    measurementId = measurementId,
                    segmentWidth = width,
                    depth = depth,
                    points = filteredPoints.map { CapturedVelocityPoint(it.velocity, it.height) },
                    selectedIndices = filteredPoints.indices.toSet()
                )
                // Clear manual points for next segment
                _manualPoints.value = emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateSegmentDimensions(segment: StreamSegment, updatedPoints: List<VelocityPoint>) {
        viewModelScope.launch {
            try {
                val result = updateSegmentUseCase(segment, updatedPoints)
                if (result is ValidationResult.Error) {
                    _error.value = result.message
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun finalizeMeasurement(name: String, notes: String) {
        val measurement = uiState.value.measurement ?: return
        val location = uiState.value.currentLocation
        
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
                _error.value = e.message
            }
        }
    }

    fun cancelMeasurement() {
        viewModelScope.launch {
            measurementRepository.deleteDraft()
        }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                val summaryUseCase = GetStreamMeasurementSummaryUseCase(app.measurementRepository, app.userRepository)
                val validator = ValidateSegmentInputUseCase()

                StreamMeasurementViewModel(
                    startMeasurementUseCase = StartStreamMeasurementUseCase(app.measurementRepository),
                    captureSegmentUseCase = CaptureStreamSegmentUseCase(),
                    completeSegmentUseCase = CompleteStreamSegmentUseCase(
                        app.measurementRepository, 
                        app.userRepository,
                        CalculateStreamSegmentUseCase()
                    ),
                    completeMeasurementUseCase = CompleteStreamMeasurementUseCase(app.measurementRepository, summaryUseCase),
                    updateSegmentUseCase = UpdateStreamSegmentUseCase(app.measurementRepository, app.userRepository, validator),
                    getSummaryUseCase = summaryUseCase,
                    bleRepository = app.bleRepository,
                    locationRepository = app.locationRepository,
                    measurementRepository = app.measurementRepository,
                    userRepository = app.userRepository
                )
            }
        }
    }
}
