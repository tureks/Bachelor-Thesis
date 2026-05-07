package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface BleRepository {
    val connectionState: Flow<BleConnectionState>
    val velocityReadings: Flow<Double>
    val batteryLevel: Flow<Int>
    val probeConnected: Flow<Boolean>
    val scannedDevices: Flow<List<Device>>

    fun startScanning()
    fun stopScanning()
    fun connect(address: String)
    fun disconnect()
    
    /**
     * Checks if Bluetooth is enabled on the device.
     */
    fun isBluetoothEnabled(): Boolean
}
