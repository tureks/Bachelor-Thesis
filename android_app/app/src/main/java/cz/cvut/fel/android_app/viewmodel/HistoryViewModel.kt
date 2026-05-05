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
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val measurements: List<StreamMeasurement> = emptyList(),
    val searchQuery: String = "",
    val selectedIds: Set<Int> = emptySet(),
    val isExporting: Boolean = false,
    val exportContent: String? = null,
    val exportedMeasurementNames: List<String> = emptyList(),
    val userEmail: String = "",
    val operatorName: String = "",
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val searchMeasurementsUseCase: SearchMeasurementsUseCase,
    private val exportStreamMeasurementUseCase: ExportStreamMeasurementUseCase,
    private val measurementRepository: StreamMeasurementRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    private val _isExporting = MutableStateFlow(false)
    private val _exportContent = MutableStateFlow<String?>(null)
    private val _exportMeta = MutableStateFlow(ExportMeta())
    private val _error = MutableStateFlow<String?>(null)

    private data class ExportMeta(
        val names: List<String> = emptyList(),
        val userEmail: String = "",
        val operatorName: String = ""
    )

    val uiState: StateFlow<HistoryUiState> = combine(
        _searchQuery.flatMapLatest { query -> searchMeasurementsUseCase(query) },
        _searchQuery,
        _selectedIds,
        _isExporting,
        _exportContent,
        _exportMeta,
        _error
    ) { args: Array<Any?> ->
        val meta = args[5] as ExportMeta
        HistoryUiState(
            measurements = args[0] as List<StreamMeasurement>,
            searchQuery = args[1] as String,
            selectedIds = args[2] as Set<Int>,
            isExporting = args[3] as Boolean,
            exportContent = args[4] as String?,
            exportedMeasurementNames = meta.names,
            userEmail = meta.userEmail,
            operatorName = meta.operatorName,
            error = args[6] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleSelection(id: Int) {
        _selectedIds.update { if (it.contains(id)) it - id else it + id }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun exportSelected() {
        val ids = _selectedIds.value.toList()
        if (ids.isEmpty()) return

        viewModelScope.launch {
            _isExporting.value = true
            try {
                val user = userRepository.user.first()
                val names = uiState.value.measurements.filter { it.id in ids }.map { it.name }
                _exportMeta.value = ExportMeta(
                    names = names,
                    userEmail = user?.email ?: "",
                    operatorName = "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim()
                )
                _exportContent.value = exportStreamMeasurementUseCase(ids)
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun exportMeasurement(measurement: StreamMeasurement) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val user = userRepository.user.first()
                _exportMeta.value = ExportMeta(
                    names = listOf(measurement.name),
                    userEmail = user?.email ?: "",
                    operatorName = "${user?.firstName ?: ""} ${user?.lastName ?: ""}".trim()
                )
                _exportContent.value = exportStreamMeasurementUseCase(measurement.id)
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
        _exportMeta.value = ExportMeta()
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
                    measurementRepository = app.measurementRepository,
                    userRepository = app.userRepository
                )
            }
        }
    }
}
