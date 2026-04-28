package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.CheckBluetoothAvailabilityUseCase
import cz.cvut.fel.android_app.domain.ConnectToDeviceUseCase
import cz.cvut.fel.android_app.domain.ObserveBleConnectionStateUseCase
import cz.cvut.fel.android_app.domain.ScanDevicesUseCase
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.model.Device
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DeviceUiState(
    val scannedDevices: List<Device> = emptyList(),
    val connectionState: BleConnectionState = BleConnectionState.Idle,
    val isScanning: Boolean = false,
    val error: String? = null
)

class DeviceViewModel(
    private val scanDevicesUseCase: ScanDevicesUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val observeBleStateUseCase: ObserveBleConnectionStateUseCase,
    private val checkBluetoothUseCase: CheckBluetoothAvailabilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceUiState())
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    init {
        // Observe scanned devices via Use Case
        scanDevicesUseCase.scannedDevices
            .onEach { devices ->
                _uiState.update { it.copy(scannedDevices = devices) }
            }
            .launchIn(viewModelScope)

        // Observe connection state via Use Case
        observeBleStateUseCase()
            .onEach { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
            .launchIn(viewModelScope)
    }

    fun startScan() {
        if (!checkBluetoothUseCase.isEnabled()) {
            _uiState.update { it.copy(error = "Bluetooth is disabled") }
            return
        }
        _uiState.update { it.copy(isScanning = true) }
        scanDevicesUseCase.startScan()
    }

    fun stopScan() {
        _uiState.update { it.copy(isScanning = false) }
        scanDevicesUseCase.stopScan()
    }

    /**
     * Toggles connection state. 
     * If already connected to THIS device, it disconnects.
     * If connected to another or idle, it connects.
     */
    fun toggleConnection(device: Device) {
        val currentState = _uiState.value.connectionState
        
        viewModelScope.launch {
            when (currentState) {
                is BleConnectionState.Connected -> {
                    if (currentState.deviceAddress == device.macAddress) {
                        connectToDeviceUseCase.disconnect()
                    } else {
                        // Switch device: disconnect current, then connect new
                        connectToDeviceUseCase.disconnect()
                        connectToDeviceUseCase(device)
                    }
                }
                else -> {
                    connectToDeviceUseCase(device)
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        scanDevicesUseCase.stopScan()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                DeviceViewModel(
                    scanDevicesUseCase = ScanDevicesUseCase(app.bleRepository),
                    connectToDeviceUseCase = ConnectToDeviceUseCase(app.bleRepository),
                    observeBleStateUseCase = ObserveBleConnectionStateUseCase(app.bleRepository),
                    checkBluetoothUseCase = CheckBluetoothAvailabilityUseCase(app.bleRepository)
                )
            }
        }
    }
}
