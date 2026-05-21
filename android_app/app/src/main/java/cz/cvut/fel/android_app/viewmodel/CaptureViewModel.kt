package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.repository.BleRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ManualVelocityPoint(val id: Long, val velocity: Double, val height: Double = 0.0)

data class CaptureUiState(
    val isCapturing: Boolean = false,
    val captureProgress: Float = 0f,
    val manualPoints: List<ManualVelocityPoint> = emptyList(),
    val currentWidth: String = "",
    val currentDepth: String = ""
)

class CaptureViewModel(
    private val bleRepository: BleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private data class CaptureState(val isCapturing: Boolean = false, val progress: Float = 0f)

    private val _captureState = MutableStateFlow(CaptureState())
    private val _manualPoints = MutableStateFlow<List<ManualVelocityPoint>>(emptyList())
    private val _currentWidth = MutableStateFlow("")
    private val _currentDepth = MutableStateFlow("")
    private val _singlePointHeight: StateFlow<Double> = userRepository.userProfile
        .map { (it?.singlePointHeight ?: 0.6) * 100.0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 60.0)
    private var captureJob: Job? = null

    val uiState: StateFlow<CaptureUiState> = combine(
        _captureState,
        _manualPoints,
        _currentWidth,
        _currentDepth
    ) { capture, points, w, d ->
        CaptureUiState(capture.isCapturing, capture.progress, points, w, d)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CaptureUiState())

    fun setCurrentWidth(width: String) { _currentWidth.value = width }
    fun setCurrentDepth(depth: String) { _currentDepth.value = depth }

    /** Adds a point using the current window average, capped to hardware max velocity. */
    fun addManualPoint(windowAverage: Double) {
        val capped = minOf(windowAverage, VELOCITY_MAX)
        _manualPoints.update { it + ManualVelocityPoint(System.currentTimeMillis(), capped, _singlePointHeight.value) }
    }

    fun updateManualPointHeight(id: Long, height: Double) {
        _manualPoints.update { list -> list.map { if (it.id == id) it.copy(height = height) else it } }
    }

    fun deleteManualPoint(id: Long) {
        _manualPoints.update { it.filterNot { p -> p.id == id } }
    }

    /** Starts an automatic capture session. Collects 100 readings and adds their average as a point. */
    fun startCapture() {
        if (_captureState.value.isCapturing) return
        _captureState.update { it.copy(isCapturing = true, progress = 0f) }
        captureJob?.cancel()
        captureJob = viewModelScope.launch {
            val readings = mutableListOf<Double>()
            bleRepository.velocityReadings
                .take(100)
                .onEach { velocity ->
                    readings.add(velocity)
                    _captureState.update { it.copy(progress = (readings.size / 100f).coerceAtMost(1f)) }
                }
                .collect()
            _captureState.update { it.copy(isCapturing = false) }
            val nonZero = readings.filter { it > 0.0 }
            val avg = if (nonZero.isEmpty()) 0.0 else nonZero.average()
            _manualPoints.update { it + ManualVelocityPoint(System.currentTimeMillis(), avg, _singlePointHeight.value) }
        }
    }

    fun cancelCapture() {
        captureJob?.cancel()
        _captureState.value = CaptureState()
    }

    /** Clears all capture state, resetting to initial. */
    fun reset() {
        captureJob?.cancel()
        _captureState.value = CaptureState()
        _manualPoints.value = emptyList()
        _currentWidth.value = ""
        _currentDepth.value = ""
    }

    /** Pre-populates state for editing an existing segment. */
    fun setForEditing(points: List<VelocityPoint>, widthMeters: Double, depthMeters: Double, unit: MeasurementUnit) {
        _manualPoints.value = points.map { ManualVelocityPoint(it.id.toLong(), it.velocity, it.measureHeight ?: 0.0) }
        _currentWidth.value = UnitConverter.metersToInput(widthMeters, unit)
        _currentDepth.value = UnitConverter.metersToInput(depthMeters, unit)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                CaptureViewModel(bleRepository = app.bleRepository, userRepository = app.userRepository)
            }
        }
    }
}