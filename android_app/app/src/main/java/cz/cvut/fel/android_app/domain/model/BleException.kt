package cz.cvut.fel.android_app.domain.model

sealed class BleException(val message: String) {
    data object DeviceNotFound : BleException("Device was not found or is out of range.")
    data object ConnectionTimeout : BleException("The connection attempt timed out.")
    data object ServicesNotSupported : BleException("This device does not support the required flow meter services.")
    data object BondingFailed : BleException("Failed to pair with the device.")
    data object HardwareError : BleException("Bluetooth hardware reported an internal error.")
    data class Unknown(val code: Int) : BleException("An unknown error occurred (Code: $code).")
}
