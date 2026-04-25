package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.Device
import cz.cvut.fel.android_app.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow

class ScanDevicesUseCase(
    private val bleRepository: BleRepository
) {
    /**
     * Returns a flow of discovered Bluetooth devices.
     */
    val scannedDevices: Flow<List<Device>> = bleRepository.scannedDevices

    /**
     * Starts scanning for BLE devices. 
     * Requires BLUETOOTH_SCAN permission (handled in UI layer).
     */
    fun startScan() {
        bleRepository.startScanning()
    }

    /**
     * Stops the active BLE scan.
     */
    fun stopScan() {
        bleRepository.stopScanning()
    }
}
