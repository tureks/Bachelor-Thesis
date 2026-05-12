package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface BleRepository {
    val connectionState: Flow<BleConnectionState>
    /** Emits velocity readings in m/s as they arrive from the connected probe. */
    val velocityReadings: Flow<Double>
    val batteryLevel: Flow<Int>
    /** True when the probe accessory is physically attached to the BLE device. */
    val probeConnected: Flow<Boolean>
    val scannedDevices: Flow<List<Device>>

    /** Clears the scanned device list and starts a new BLE scan. */
    fun startScanning()
    /** Stops the ongoing BLE scan. */
    fun stopScanning()
    /**
     * Initiates a GATT connection to the device with the given MAC [address].
     * No-op if a connection is already open.
     */
    fun connect(address: String)
    /** Requests disconnection from the currently connected GATT device. */
    fun disconnect()
    /** Returns true if Bluetooth is enabled on the device. */
    fun isBluetoothEnabled(): Boolean
}
