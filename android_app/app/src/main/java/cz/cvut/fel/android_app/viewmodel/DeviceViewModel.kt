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
import cz.cvut.fel.android_app.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DeviceUiState(
    val scannedDevices: List<Device> = emptyList(),
    val knownDevices: List<Device> = emptyList(),
    val connectionState: BleConnectionState = BleConnectionState.Idle,
    val connectingAddress: String? = null,
    val isScanning: Boolean = false,
    val hasScanned: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val error: String? = null
)

class DeviceViewModel(
    private val bleRepository: BleRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    init {
        bleRepository.clearScannedDevices()

        bleRepository.scannedDevices
            .onEach { devices -> _uiState.update { it.copy(scannedDevices = devices) } }
            .launchIn(viewModelScope)

        bleRepository.connectionState
            .onEach { state ->
                _uiState.update { current ->
                    current.copy(
                        connectionState = state,
                        connectingAddress = if (state is BleConnectionState.Connecting) current.connectingAddress else null,
                        error = if (state is BleConnectionState.Error) state.exception.message else current.error
                    )
                }
            }
            .launchIn(viewModelScope)

        deviceRepository.getAll()
            .onEach { devices -> _uiState.update { it.copy(knownDevices = devices) } }
            .launchIn(viewModelScope)
    }

    fun startScan() {
        if (!bleRepository.isBluetoothEnabled()) {
            _uiState.update { it.copy(isBluetoothEnabled = false) }
            return
        }
        _uiState.update { it.copy(isScanning = true, hasScanned = true, isBluetoothEnabled = true, error = null) }
        bleRepository.startScanning()
    }

    fun stopScan() {
        _uiState.update { it.copy(isScanning = false) }
        bleRepository.stopScanning()
    }

    /**
     * Connects to [device] or disconnects if it is already the active device.
     */
    fun toggleConnection(device: Device) {
        val currentState = _uiState.value.connectionState
        viewModelScope.launch {
            when (currentState) {
                is BleConnectionState.Connected -> {
                    if (currentState.deviceAddress == device.macAddress) {
                        bleRepository.disconnect()
                    } else {
                        _uiState.update { it.copy(connectingAddress = device.macAddress) }
                        bleRepository.disconnect()
                        bleRepository.connect(device.macAddress)
                    }
                }
                else -> {
                    _uiState.update { it.copy(connectingAddress = device.macAddress) }
                    bleRepository.connect(device.macAddress)
                }
            }
        }
    }

    fun refreshBluetoothState() {
        _uiState.update { it.copy(isBluetoothEnabled = bleRepository.isBluetoothEnabled()) }
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
                DeviceViewModel(
                    bleRepository = app.bleRepository,
                    deviceRepository = app.deviceRepository
                )
            }
        }
    }
}