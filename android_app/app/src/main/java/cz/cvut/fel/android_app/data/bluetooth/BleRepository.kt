package cz.cvut.fel.android_app.data.bluetooth

import kotlinx.coroutines.flow.Flow

interface BleRepository {

    // Current connection state
    val connectionState: Flow<BleConnectionState>

    // Stream of velocity readings
    val velocityReadings: Flow<Double>

    // Connects to the BLE device with the MAC
    fun connect(address: String)

    // Closes the connection
    fun disconnect()
}