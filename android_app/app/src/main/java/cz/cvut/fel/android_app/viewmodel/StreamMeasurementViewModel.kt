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
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
    val currentLocation: Location? = null,
    val connectionState: BleConnectionState = BleConnectionState.Idle,
    val batteryLevel: Int = 0,
    val probeConnected: Boolean = false,
    val timeWindow: Int = 10,
    val windowAverage: Double = 0.0,
    val windowMin: Double? = null,
    val windowMax: Double? = null,
    val recentReadings: List<VelocityReading> = emptyList(),
    val manualPoints: List<ManualVelocityPoint> = emptyList(),
    val velocityOverLimit: Boolean = false,
    val currentWidth: String = "",
    val currentDepth: String = "",
    val editingSegment: StreamSegment? = null,
    val editingPoints: List<VelocityPoint> = emptyList(),
    val preferredUnit: MeasurementUnit = MeasurementUnit.HYDROMETRIC,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class StreamMeasurementViewModel(
    private val startMeasurementUseCase: StartStreamMeasurementUseCase,
    private val completeSegmentUseCase: CompleteStreamSegmentUseCase,
    private val completeMeasurementUseCase: CompleteStreamMeasurementUseCase,
    private val updateSegmentUseCase: UpdateStreamSegmentUseCase,
    private val updateMeasurementUseCase: UpdateStreamMeasurementUseCase,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase,
    private val bleRepository: BleRepository,
    private val locationRepository: LocationRepository,
    private val measurementRepository: StreamMeasurementRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    private val _timeWindow = MutableStateFlow(10)
    private var _singlePointHeightPercent: Double = 60.0
    private val _manualPoints = MutableStateFlow<List<ManualVelocityPoint>>(emptyList())
    private val _currentWidth = MutableStateFlow("")
    private val _currentDepth = MutableStateFlow("")
    private val _editingSegment = MutableStateFlow<StreamSegment?>(null)
    private val _editingPoints = MutableStateFlow<List<VelocityPoint>>(emptyList())
    private val _captureState = MutableStateFlow(CaptureState())

    private data class CaptureState(
        val isCapturing: Boolean = false,
        val progress: Float = 0f
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
        val probeConnected: Boolean,
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
        }.onStart { emit(MeasurementData(null, emptyList(), null)) }

    private val hardwareFlow: Flow<HardwareState> = combine(
        bleRepository.velocityReadings.onStart { emit(0.0) },
        bleRepository.connectionState,
        bleRepository.batteryLevel,
        bleRepository.probeConnected,
        locationRepository.observeLocation().onStart { emit(null) }
    ) { velocity, connection, battery, probe, location ->
        HardwareState(velocity, connection, battery, probe, location)
    }

    private val userUnitFlow: Flow<MeasurementUnit> = userRepository.userProfile
        .map { it?.preferredUnit ?: MeasurementUnit.HYDROMETRIC }
        .onStart { emit(MeasurementUnit.HYDROMETRIC) }
        .distinctUntilChanged()

    private val recentReadingsFlow: Flow<List<VelocityReading>> = bleRepository.velocityReadings
        .scan(emptyList<VelocityReading>()) { accumulator, velocity ->
            val now = System.currentTimeMillis()
            val windowStart = now - (_timeWindow.value * 1000L)
            (accumulator + VelocityReading(velocity, now)).filter { it.timestamp >= windowStart }
        }
        .onStart { emit(emptyList()) }

    @OptIn(FlowPreview::class)
    private val windowAverageFlow: Flow<Double> = combine(recentReadingsFlow, _timeWindow) { readings, window ->
        val now = System.currentTimeMillis()
        val windowStart = now - window * 1000L
        val windowed = readings.filter { it.timestamp >= windowStart && it.velocity > 0.0 }
        if (windowed.isEmpty()) 0.0 else windowed.map { it.velocity }.average()
    }
    .sample(1200L)
    .distinctUntilChanged()
    .onStart { emit(0.0) }

    init {
        userRepository.userProfile
            .onEach { profile -> _singlePointHeightPercent = (profile?.singlePointHeight ?: 0.6) * 100.0 }
            .launchIn(viewModelScope)
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<StreamMeasurementUiState> = combine(
        measurementDataFlow,
        hardwareFlow,
        _captureState,
        recentReadingsFlow,
        _timeWindow,
        _manualPoints,
        _currentWidth,
        _currentDepth,
        _editingSegment,
        _editingPoints,
        userUnitFlow,
        _error,
        windowAverageFlow
    ) { args ->
        val data = args[0] as MeasurementData
        val hardware = args[1] as HardwareState
        val capture = args[2] as CaptureState
        val readings = args[3] as List<VelocityReading>
        val window = args[4] as Int
        val points = args[5] as List<ManualVelocityPoint>
        val currentWidth = args[6] as String
        val currentDepth = args[7] as String
        val editingSegment = args[8] as StreamSegment?
        val editingPoints = args[9] as List<VelocityPoint>
        val unit = args[10] as MeasurementUnit
        val error = args[11] as String?
        val windowAvg = args[12] as Double

        val windowStart = System.currentTimeMillis() - window * 1000L
        val liveReadings = readings.filter { it.timestamp >= windowStart }
        val validReadings = liveReadings.filter { it.velocity > 0.0 }

        StreamMeasurementUiState(
            measurement = data.draft,
            segments = data.segments,
            totals = data.totals,
            currentVelocity = hardware.velocity,
            connectionState = hardware.connectionState,
            batteryLevel = hardware.batteryLevel,
            probeConnected = hardware.probeConnected,
            currentLocation = hardware.currentLocation,
            isCapturing = capture.isCapturing,
            captureProgress = capture.progress,
            timeWindow = window,
            recentReadings = readings,
            windowAverage = windowAvg,
            windowMin = validReadings.minOfOrNull { it.velocity } ?: 0.0,
            windowMax = validReadings.maxOfOrNull { it.velocity } ?: 0.0,
            velocityOverLimit = hardware.velocity > VELOCITY_MAX,
            manualPoints = points,
            currentWidth = currentWidth,
            currentDepth = currentDepth,
            editingSegment = editingSegment,
            editingPoints = editingPoints,
            preferredUnit = unit,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreamMeasurementUiState())

    fun setCurrentWidth(width: String) {
        _currentWidth.value = width
    }

    fun setCurrentDepth(depth: String) {
        _currentDepth.value = depth
    }

    fun startEditingSegment(segment: StreamSegment) {
        _editingSegment.value = segment
        viewModelScope.launch {
            val points = measurementRepository.getVelocityPoints(segment.id)
            _editingPoints.value = points
            _manualPoints.value = points.map { ManualVelocityPoint(it.id.toLong(), it.velocity, it.measureHeight ?: 0.0) }
            
            val unit = uiState.value.preferredUnit
            _currentWidth.value = UnitConverter.metersToInput(segment.segmentWidth, unit)
            _currentDepth.value = UnitConverter.metersToInput(segment.depth, unit)
        }
    }

    fun stopEditingSegment() {
        _editingSegment.value = null
        _editingPoints.value = emptyList()
        _currentWidth.value = ""
        _currentDepth.value = ""
    }

    fun setTimeWindow(seconds: Int) {
        _timeWindow.value = seconds
    }

    /**
     * Adds a manual velocity point using the current [windowAverage], capped to [VELOCITY_MAX],
     * at the measurement height configured in the user profile ([_singlePointHeightPercent]).
     */
    fun addManualPoint() {
        val avg = uiState.value.windowAverage
        val capped = minOf(avg, VELOCITY_MAX)
        _manualPoints.update { it + ManualVelocityPoint(System.currentTimeMillis(), capped, height = _singlePointHeightPercent) }
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

    /** Snapshots the current GPS location, then starts a new draft measurement and resets segment input state. */
    fun startNewMeasurement() {
        viewModelScope.launch {
            try {
                val location = locationRepository.getCurrentLocation()
                startMeasurementUseCase()
                
                measurementRepository.getDraft()?.let { draft ->
                    if (location != null) {
                        measurementRepository.update(draft.copy(
                            gpsLat = location.latitude,
                            gpsLong = location.longitude
                        ))
                    }
                }

                _currentWidth.value = ""
                _currentDepth.value = ""
                _manualPoints.value = emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Starts an automatic velocity capture session. Raw readings are collected in parallel with
     * [CaptureStreamSegmentUseCase]; progress is reported as `readings.size / 100`. Once 100
     * readings are accumulated, the session ends and their average is added as a manual point
     * at the configured single-point height.
     */
    fun startCapture() {
        if (_captureState.value.isCapturing) return
        _captureState.update { it.copy(isCapturing = true, progress = 0f) }

        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            val readings = mutableListOf<Double>()
            bleRepository.velocityReadings.collect { velocity ->
                readings.add(velocity)
                val progress = readings.size / 100f
                _captureState.update { it.copy(progress = progress.coerceAtMost(1f)) }
                if (progress >= 1f) {
                    _captureState.update { it.copy(isCapturing = false) }
                    _manualPoints.update { it + ManualVelocityPoint(System.currentTimeMillis(), readings.average(), height = _singlePointHeightPercent) }
                    cancel()
                }
            }
        }
    }

    /**
     * Saves the current segment to the database asynchronously and invokes [onComplete] on success.
     * @param width segment width in display units
     * @param depth water depth in display units
     * @param selectedPointIds IDs of [ManualVelocityPoint]s included in the velocity average
     */
    fun completeSegment(width: Double, depth: Double, selectedPointIds: Set<Long>, onComplete: () -> Unit = {}) {
        val measurementId = uiState.value.measurement?.id ?: return
        val editingSegment = uiState.value.editingSegment
        val points = uiState.value.manualPoints

        if (editingSegment == null && points.isEmpty()) {
            _error.value = "No velocity data captured."
            return
        }

        val unit = uiState.value.preferredUnit
        val widthMetric = UnitConverter.displayToMeters(width, unit)
        val depthMetric = UnitConverter.displayToMeters(depth, unit)

        viewModelScope.launch {
            try {
                if (editingSegment != null) {
                    val filteredPoints = points.filter { selectedPointIds.contains(it.id) }
                    updateSegmentUseCase(
                        editingSegment.copy(segmentWidth = widthMetric, depth = depthMetric),
                        filteredPoints.map { VelocityPoint(segmentId = editingSegment.id, velocity = it.velocity, measureHeight = it.height) }
                    )
                    _editingSegment.value = null
                    _manualPoints.value = emptyList()
                } else {
                    val filteredPoints = points.filter { selectedPointIds.contains(it.id) }
                    if (filteredPoints.isEmpty()) {
                        _error.value = "Please select at least one velocity point."
                        return@launch
                    }
                    completeSegmentUseCase(
                        measurementId = measurementId,
                        segmentWidth = widthMetric,
                        depth = depthMetric,
                        points = filteredPoints.map { CapturedVelocityPoint(it.velocity, it.height) }
                    )
                    _manualPoints.value = emptyList()
                }
                _currentWidth.value = ""
                _currentDepth.value = ""
                onComplete()
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
                _error.value = e.message ?: "Update failed"
            }
        }
    }

    /**
     * Finalizes the active draft measurement. Attempts to fetch fresh GPS fix and uses it.
     */
    fun finalizeMeasurement(name: String, notes: String) {
        val measurement = uiState.value.measurement ?: return
        
        viewModelScope.launch {
            try {
                val location = locationRepository.getCurrentLocation()
                completeMeasurementUseCase(
                    measurementId = measurement.id,
                    name = name,
                    note = notes,
                    gpsLat = location?.latitude ?: measurement.gpsLat,
                    gpsLong = location?.longitude ?: measurement.gpsLong
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun cancelMeasurement() {
        viewModelScope.launch {
            measurementRepository.deleteDraft()
            _manualPoints.value = emptyList()
            _currentWidth.value = ""
            _currentDepth.value = ""
            _editingSegment.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        const val VELOCITY_MAX = 5.0

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                val summaryUseCase = GetStreamMeasurementSummaryUseCase(app.measurementRepository)
                val validator = ValidateSegmentInputUseCase()

                StreamMeasurementViewModel(
                    startMeasurementUseCase = StartStreamMeasurementUseCase(app.measurementRepository),
                    completeSegmentUseCase = CompleteStreamSegmentUseCase(
                        app.measurementRepository,
                        CalculateStreamSegmentUseCase()
                    ),
                    completeMeasurementUseCase = CompleteStreamMeasurementUseCase(app.measurementRepository, summaryUseCase),
                    updateSegmentUseCase = UpdateStreamSegmentUseCase(
                        app.measurementRepository,
                        validator,
                        summaryUseCase
                    ),
                    updateMeasurementUseCase = UpdateStreamMeasurementUseCase(app.measurementRepository),
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
