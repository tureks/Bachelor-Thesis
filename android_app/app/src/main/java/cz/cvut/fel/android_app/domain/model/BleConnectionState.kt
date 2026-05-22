package cz.cvut.fel.android_app.domain.model

/** BLE connection lifecycle: [Idle] → [Connecting] → [Connected] → [Disconnected] or [Error]. */
sealed class BleConnectionState {
    data object Idle : BleConnectionState()
    data object Connecting : BleConnectionState()
    data class Connected(val deviceAddress: String) : BleConnectionState()
    data object Disconnected : BleConnectionState()
    data class Error(val exception: BleException) : BleConnectionState()
}
