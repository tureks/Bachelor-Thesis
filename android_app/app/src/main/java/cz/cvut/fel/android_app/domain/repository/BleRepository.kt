package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.BleConnectionState
import kotlinx.coroutines.flow.Flow

interface BleRepository {
    val connectionState: Flow<BleConnectionState>
    val velocityReadings: Flow<Double>
    fun connect(address: String)
    fun disconnect()
}