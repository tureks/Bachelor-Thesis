package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.CalculateStreamSegmentUseCase
import cz.cvut.fel.android_app.domain.CompleteStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.CompleteStreamSegmentUseCase
import cz.cvut.fel.android_app.domain.GetStreamMeasurementSummaryUseCase
import cz.cvut.fel.android_app.domain.StartStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.UpdateStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.UpdateStreamSegmentUseCase
import cz.cvut.fel.android_app.domain.ValidateSegmentInputUseCase
import cz.cvut.fel.android_app.domain.model.CapturedVelocityPoint
import cz.cvut.fel.android_app.domain.model.Location
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.model.StreamMeasurementTotals
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.ValidationResult
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.repository.LocationRepository
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeasurementUiState(
    val measurement: StreamMeasurement? = null,
    val segments: List<StreamSegment> = emptyList(),
    val totals: StreamMeasurementTotals? = null,
    val editingSegment: StreamSegment? = null,
    val editingPoints: List<VelocityPoint> = emptyList(),
    val preferredUnit: MeasurementUnit = MeasurementUnit.HYDROMETRIC,
    val currentLocation: Location? = null,
    val error: String? = null
)

/**
 * Active DRAFT measurement lifecycle: start → add segments → finalize or cancel.
 * segment/totals flows re-subscribe on draft changes.
 * [completeSegment] handles both new segments and edits depending on [MeasurementUiState.editingSegment].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MeasurementViewModel(
    private val startMeasurementUseCase: StartStreamMeasurementUseCase,
    private val completeSegmentUseCase: CompleteStreamSegmentUseCase,
    private val completeMeasurementUseCase: CompleteStreamMeasurementUseCase,
    private val updateSegmentUseCase: UpdateStreamSegmentUseCase,
    private val updateMeasurementUseCase: UpdateStreamMeasurementUseCase,
    private val getSummaryUseCase: GetStreamMeasurementSummaryUseCase,
    private val measurementRepository: StreamMeasurementRepository,
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _editingSegment = MutableStateFlow<StreamSegment?>(null)
    private val _editingPoints = MutableStateFlow<List<VelocityPoint>>(emptyList())
    private val _currentLocation = MutableStateFlow<Location?>(null)
    private val _error = MutableStateFlow<String?>(null)

    private data class MeasurementData(
        val draft: StreamMeasurement?,
        val segments: List<StreamSegment>,
        val totals: StreamMeasurementTotals?
    )

    private val measurementDataFlow: Flow<MeasurementData> =
        measurementRepository.getDraftFlow().flatMapLatest { draft ->
            if (draft != null) {
                measurementRepository.getSegmentsFlow(draft.id).map { segments ->
                    MeasurementData(draft, segments, getSummaryUseCase(draft.id))
                }
            } else {
                flowOf(MeasurementData(null, emptyList(), null))
            }
        }.onStart { emit(MeasurementData(null, emptyList(), null)) }

    private val userUnitFlow: Flow<MeasurementUnit> = userRepository.userProfile
        .map { it?.preferredUnit ?: MeasurementUnit.HYDROMETRIC }
        .onStart { emit(MeasurementUnit.HYDROMETRIC) }
        .distinctUntilChanged()

    private data class EditingState(val segment: StreamSegment?, val points: List<VelocityPoint>)
    private data class MetaState(val unit: MeasurementUnit, val location: Location?, val error: String?)

    private val editingState = combine(_editingSegment, _editingPoints) { s, p -> EditingState(s, p) }
    private val metaState = combine(userUnitFlow, _currentLocation, _error) { u, l, e -> MetaState(u, l, e) }

    val uiState: StateFlow<MeasurementUiState> = combine(measurementDataFlow, editingState, metaState) { data, editing, meta ->
        MeasurementUiState(
            measurement = data.draft,
            segments = data.segments,
            totals = data.totals,
            editingSegment = editing.segment,
            editingPoints = editing.points,
            preferredUnit = meta.unit,
            currentLocation = meta.location,
            error = meta.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MeasurementUiState())

    init {
        viewModelScope.launch {
            _currentLocation.value = locationRepository.getCurrentLocation()
        }
        locationRepository.observeLocation()
            .onEach { _currentLocation.value = it }
            .launchIn(viewModelScope)
    }

    /** Deletes any existing draft, captures the current GPS fix, and creates a new measurement. */
    fun startNewMeasurement() {
        viewModelScope.launch {
            try {
                val location = _currentLocation.value ?: locationRepository.getCurrentLocation()
                startMeasurementUseCase()
                measurementRepository.getDraft()?.let { draft ->
                    if (location != null) {
                        measurementRepository.update(draft.copy(gpsLat = location.latitude, gpsLong = location.longitude))
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Saves the current segment and invokes [onComplete] on success.
     * @param width segment width in display units
     * @param depth water depth in display units
     * @param points selected velocity points to include in the average
     */
    fun completeSegment(width: Double, depth: Double, points: List<ManualVelocityPoint>, onComplete: () -> Unit = {}) {
        val measurementId = uiState.value.measurement?.id ?: return
        val editingSegment = uiState.value.editingSegment

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
                    updateSegmentUseCase(
                        editingSegment.copy(segmentWidth = widthMetric, depth = depthMetric),
                        points.map { VelocityPoint(segmentId = editingSegment.id, velocity = it.velocity, measureHeight = it.height) }
                    )
                    _editingSegment.value = null
                } else {
                    if (points.isEmpty()) {
                        _error.value = "Please select at least one velocity point."
                        return@launch
                    }
                    completeSegmentUseCase(
                        measurementId = measurementId,
                        segmentWidth = widthMetric,
                        depth = depthMetric,
                        points = points.map { CapturedVelocityPoint(it.velocity, it.height) }
                    )
                }
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
                if (result is ValidationResult.Error) _error.value = result.message
            } catch (e: Exception) {
                _error.value = e.message ?: "Update failed"
            }
        }
    }

    /** Finalizes the active draft measurement. Attempts to fetch a fresh GPS fix before saving. */
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
            _editingSegment.value = null
        }
    }

    fun startEditingSegment(segment: StreamSegment) {
        _editingSegment.value = segment
        viewModelScope.launch {
            _editingPoints.value = measurementRepository.getVelocityPoints(segment.id)
        }
    }

    fun stopEditingSegment() {
        _editingSegment.value = null
        _editingPoints.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                val summaryUseCase = GetStreamMeasurementSummaryUseCase(app.measurementRepository)
                val validator = ValidateSegmentInputUseCase()
                MeasurementViewModel(
                    startMeasurementUseCase = StartStreamMeasurementUseCase(app.measurementRepository),
                    completeSegmentUseCase = CompleteStreamSegmentUseCase(app.measurementRepository, CalculateStreamSegmentUseCase()),
                    completeMeasurementUseCase = CompleteStreamMeasurementUseCase(app.measurementRepository, summaryUseCase),
                    updateSegmentUseCase = UpdateStreamSegmentUseCase(app.measurementRepository, validator, summaryUseCase),
                    updateMeasurementUseCase = UpdateStreamMeasurementUseCase(app.measurementRepository),
                    getSummaryUseCase = summaryUseCase,
                    measurementRepository = app.measurementRepository,
                    locationRepository = app.locationRepository,
                    userRepository = app.userRepository
                )
            }
        }
    }
}