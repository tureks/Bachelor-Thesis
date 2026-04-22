package cz.cvut.fel.android_app.data.bluetooth

sealed class BleConnectionState {

    data object Idle : BleConnectionState()

    // waiting for the GATT callback
    data object Connecting : BleConnectionState()

    // GATT connected and services discovered
    data class Connected(val deviceAddress: String) : BleConnectionState()

    // The connection was intentionally closed by the app
    data object Disconnected : BleConnectionState()

    // The connection was lost unexpectedly - provided reason error code
    data class Error(val reason: Int) : BleConnectionState()
}