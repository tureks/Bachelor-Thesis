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
import kotlinx.coroutines.flow.*

data class ManualVelocityPoint(val id: Long, val velocity: Double, val height: Double = 0.0)

data class CaptureUiState(
    val manualPoints: List<ManualVelocityPoint> = emptyList(),
    val currentWidth: String = "",
    val currentDepth: String = ""
)

/** Manages velocity point capture for a single segment. */
class CaptureViewModel(
    private val bleRepository: BleRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _manualPoints = MutableStateFlow<List<ManualVelocityPoint>>(emptyList())
    private val _currentWidth = MutableStateFlow("")
    private val _currentDepth = MutableStateFlow("")
    private val _singlePointHeight: StateFlow<Double> = userRepository.userProfile
        .map { (it?.singlePointHeight ?: 0.6) * 100.0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 60.0)

    val uiState: StateFlow<CaptureUiState> = combine(
        _manualPoints,
        _currentWidth,
        _currentDepth
    ) { points, w, d ->
        CaptureUiState(points, w, d)
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

    /** Clears all state, ready for a new segment. */
    fun reset() {
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