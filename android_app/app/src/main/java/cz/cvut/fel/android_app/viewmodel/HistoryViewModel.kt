package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.ExportStreamMeasurementUseCase
import cz.cvut.fel.android_app.domain.SearchMeasurementsUseCase
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val measurements: List<StreamMeasurement> = emptyList(),
    val searchQuery: String = "",
    val isExporting: Boolean = false,
    val exportContent: String? = null,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val searchMeasurementsUseCase: SearchMeasurementsUseCase,
    private val exportStreamMeasurementUseCase: ExportStreamMeasurementUseCase,
    private val measurementRepository: StreamMeasurementRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isExporting = MutableStateFlow(false)
    private val _exportContent = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        _searchQuery.flatMapLatest { query -> searchMeasurementsUseCase(query) },
        _searchQuery,
        _isExporting,
        _exportContent,
        _error
    ) { measurements, query, exporting, content, error ->
        HistoryUiState(
            measurements = measurements,
            searchQuery = query,
            isExporting = exporting,
            exportContent = content,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun exportMeasurement(measurement: StreamMeasurement) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val csvContent = exportStreamMeasurementUseCase(measurement.id)
                _exportContent.value = csvContent
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun deleteMeasurement(measurement: StreamMeasurement) {
        viewModelScope.launch {
            try {
                measurementRepository.deleteById(measurement.id)
            } catch (e: Exception) {
                _error.value = "Delete failed: ${e.message}"
            }
        }
    }

    fun clearExportContent() {
        _exportContent.value = null
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                HistoryViewModel(
                    searchMeasurementsUseCase = SearchMeasurementsUseCase(app.measurementRepository),
                    exportStreamMeasurementUseCase = ExportStreamMeasurementUseCase(
                        app.measurementRepository,
                        app.userRepository
                    ),
                    measurementRepository = app.measurementRepository
                )
            }
        }
    }
}
