package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.ExportStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.GetStreamMeasurementSummaryUseCase
import cz.cvut.fel.android_app.domain.UpdateStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.UpdateStreamSegmentUseCase
import cz.cvut.fel.android_app.domain.ValidateSegmentInputUseCase
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.ValidationResult
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeasurementDetailUiState(
    val measurement: StreamMeasurement? = null,
    val segments: List<StreamSegment> = emptyList(),
    val editingSegment: StreamSegment? = null,
    val editingPoints: List<VelocityPoint> = emptyList(),
    val preferredUnit: MeasurementUnit = MeasurementUnit.HYDROMETRIC,
    val exportContent: String? = null,
    val downloadContent: String? = null,
    val exportedMeasurementNames: List<String> = emptyList(),
    val userEmail: String = "",
    val operatorName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class MeasurementDetailViewModel(
    private val measurementRepository: StreamMeasurementRepository,
    private val userRepository: UserRepository,
    private val exportUseCase: ExportStreamMeasurementUseCase,
    private val updateSegmentUseCase: UpdateStreamSegmentUseCase,
    private val updateMeasurementUseCase: UpdateStreamMeasurementUseCase
) : ViewModel() {

    private data class ContentState(
        val measurement: StreamMeasurement? = null,
        val segments: List<StreamSegment> = emptyList(),
        val editingSegment: StreamSegment? = null,
        val editingPoints: List<VelocityPoint> = emptyList()
    )

    private data class ExportState(
        val content: String? = null,
        val downloadContent: String? = null,
        val names: List<String> = emptyList(),
        val userEmail: String = "",
        val operatorName: String = ""
    )

    private val _content = MutableStateFlow(ContentState())
    private val _export = MutableStateFlow(ExportState())
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val unitFlow = userRepository.userProfile
        .map { it?.preferredUnit ?: MeasurementUnit.HYDROMETRIC }
        .onStart { emit(MeasurementUnit.HYDROMETRIC) }

    val uiState: StateFlow<MeasurementDetailUiState> = combine(
        _content,
        _export,
        _isLoading,
        _error,
        unitFlow
    ) { content, export, isLoading, error, unit ->
        MeasurementDetailUiState(
            measurement = content.measurement,
            segments = content.segments,
            editingSegment = content.editingSegment,
            editingPoints = content.editingPoints,
            preferredUnit = unit,
            exportContent = export.content,
            downloadContent = export.downloadContent,
            exportedMeasurementNames = export.names,
            userEmail = export.userEmail,
            operatorName = export.operatorName,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MeasurementDetailUiState())

    fun loadMeasurement(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _content.update {
                    it.copy(
                        measurement = measurementRepository.getById(id),
                        segments = measurementRepository.getSegments(id)
                    )
                }
            } catch (e: Exception) {
                _error.value = "Failed to load measurement: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startEditingSegment(segment: StreamSegment) {
        _content.update { it.copy(editingSegment = segment) }
        viewModelScope.launch {
            val points = measurementRepository.getVelocityPoints(segment.id)
            _content.update { it.copy(editingPoints = points) }
        }
    }

    fun stopEditingSegment() {
        _content.update { it.copy(editingSegment = null, editingPoints = emptyList()) }
    }

    fun updateSegmentDimensions(segment: StreamSegment, updatedPoints: List<VelocityPoint>) {
        viewModelScope.launch {
            try {
                val result = updateSegmentUseCase(segment, updatedPoints)
                if (result is ValidationResult.Error) {
                    _error.value = result.message
                } else {
                    val id = segment.measurementId
                    _content.update {
                        it.copy(
                            segments = measurementRepository.getSegments(id),
                            measurement = measurementRepository.getById(id)
                        )
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Update failed"
            }
        }
    }

    fun updateMetadata(name: String, note: String) {
        val id = _content.value.measurement?.id ?: return
        viewModelScope.launch {
            try {
                updateMeasurementUseCase(id, name, note.ifBlank { null })
                _content.update { it.copy(measurement = measurementRepository.getById(id)) }
            } catch (e: Exception) {
                _error.value = e.message ?: "Update failed"
            }
        }
    }

    fun exportMeasurement() {
        if (_content.value.measurement == null) return
        viewModelScope.launch {
            try { _export.update { it.copy(content = generateExportContent()) } }
            catch (e: Exception) { _error.value = "Export failed: ${e.message ?: "Unknown error"}" }
        }
    }

    fun downloadMeasurement() {
        if (_content.value.measurement == null) return
        viewModelScope.launch {
            try { _export.update { it.copy(downloadContent = generateExportContent()) } }
            catch (e: Exception) { _error.value = "Export failed: ${e.message ?: "Unknown error"}" }
        }
    }

    private suspend fun generateExportContent(): String {
        val measurement = _content.value.measurement ?: error("No measurement loaded")
        val profile = userRepository.userProfile.first()
        val unit = uiState.value.preferredUnit
        val operatorName = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}".trim()
        val email = profile?.email ?: ""
        _export.update { it.copy(names = listOf(measurement.name), userEmail = email, operatorName = operatorName) }
        return exportUseCase(measurement.id, unit, operatorName, email)
    }

    fun clearDownloadContent() {
        _export.update { it.copy(downloadContent = null) }
    }

    fun deleteMeasurement(onDeleted: () -> Unit) {
        val id = _content.value.measurement?.id ?: return
        viewModelScope.launch {
            try {
                measurementRepository.deleteById(id)
                onDeleted()
            } catch (e: Exception) {
                _error.value = "Delete failed: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun clearExportContent() {
        _export.value = ExportState()
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
                MeasurementDetailViewModel(
                    measurementRepository = app.measurementRepository,
                    userRepository = app.userRepository,
                    exportUseCase = ExportStreamMeasurementUseCase(app.measurementRepository),
                    updateSegmentUseCase = UpdateStreamSegmentUseCase(
                        app.measurementRepository,
                        validator,
                        summaryUseCase
                    ),
                    updateMeasurementUseCase = UpdateStreamMeasurementUseCase(app.measurementRepository)
                )
            }
        }
    }
}