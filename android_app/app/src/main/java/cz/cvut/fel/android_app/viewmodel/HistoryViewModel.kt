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
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 6

data class HistoryUiState(
    val measurements: List<StreamMeasurement> = emptyList(),
    val hasMore: Boolean = false,
    val searchQuery: String = "",
    val selectedIds: Set<Int> = emptySet(),
    val isExporting: Boolean = false,
    val exportContent: String? = null,
    val exportedMeasurementNames: List<String> = emptyList(),
    val userEmail: String = "",
    val operatorName: String = "",
    val preferredUnit: MeasurementUnit = MeasurementUnit.HYDROMETRIC,
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
    private val _displayCount = MutableStateFlow(PAGE_SIZE)
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

    private val allMeasurementsFlow: Flow<List<StreamMeasurement>> =
        _searchQuery.flatMapLatest { query -> searchMeasurementsUseCase(query) }

    val uiState: StateFlow<HistoryUiState> = combine(
        allMeasurementsFlow,
        _searchQuery,
        _displayCount,
        _selectedIds,
        _isExporting,
        _exportContent,
        _exportMeta,
        _error,
        userRepository.userProfile.map { it?.preferredUnit ?: MeasurementUnit.HYDROMETRIC }.onStart { emit(MeasurementUnit.HYDROMETRIC) }
    ) { args: Array<Any?> ->
        val all = args[0] as List<StreamMeasurement>
        val query = args[1] as String
        val displayCount = args[2] as Int
        val meta = args[6] as ExportMeta
        @Suppress("UNCHECKED_CAST")
        HistoryUiState(
            measurements = if (query.isBlank()) all.take(displayCount) else all,
            hasMore = query.isBlank() && all.size > displayCount,
            searchQuery = query,
            selectedIds = args[3] as Set<Int>,
            isExporting = args[4] as Boolean,
            exportContent = args[5] as String?,
            exportedMeasurementNames = meta.names,
            userEmail = meta.userEmail,
            operatorName = meta.operatorName,
            preferredUnit = args[8] as MeasurementUnit,
            error = args[7] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _displayCount.value = PAGE_SIZE
    }

    fun loadMore() {
        _displayCount.update { it + PAGE_SIZE }
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
                val profile = userRepository.userProfile.first()
                val names = uiState.value.measurements.filter { it.id in ids }.map { it.name }
                _exportMeta.value = ExportMeta(
                    names = names,
                    userEmail = profile?.email ?: "",
                    operatorName = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}".trim()
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
                val profile = userRepository.userProfile.first()
                _exportMeta.value = ExportMeta(
                    names = listOf(measurement.name),
                    userEmail = profile?.email ?: "",
                    operatorName = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}".trim()
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