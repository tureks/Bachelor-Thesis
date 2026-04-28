package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.model.Device
import cz.cvut.fel.android_app.domain.repository.BleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DeviceUiState(
    val scannedDevices: List<Device> = emptyList(),
    val connectionState: BleConnectionState = BleConnectionState.Idle,
    val isScanning: Boolean = false,
    val error: String? = null
)

class DeviceViewModel(
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    init {
        // Direct observation of Repository flows
        bleRepository.scannedDevices
            .onEach { devices ->
                _uiState.update { it.copy(scannedDevices = devices) }
            }
            .launchIn(viewModelScope)

        bleRepository.connectionState
            .onEach { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
            .launchIn(viewModelScope)
    }

    fun startScan() {
        if (!bleRepository.isBluetoothEnabled()) {
            _uiState.update { it.copy(error = "Bluetooth is disabled") }
            return
        }
        _uiState.update { it.copy(isScanning = true) }
        bleRepository.startScanning()
    }

    fun stopScan() {
        _uiState.update { it.copy(isScanning = false) }
        bleRepository.stopScanning()
    }

    fun toggleConnection(device: Device) {
        val currentState = _uiState.value.connectionState
        
        viewModelScope.launch {
            when (currentState) {
                is BleConnectionState.Connected -> {
                    if (currentState.deviceAddress == device.macAddress) {
                        bleRepository.disconnect()
                    } else {
                        bleRepository.disconnect()
                        bleRepository.connect(device.macAddress)
                    }
                }
                else -> {
                    bleRepository.connect(device.macAddress)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        bleRepository.stopScanning()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                DeviceViewModel(bleRepository = app.bleRepository)
            }
        }
    }
}
