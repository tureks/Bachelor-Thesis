package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.Device
import cz.cvut.fel.android_app.domain.repository.BleRepository

class ConnectToDeviceUseCase(
    private val bleRepository: BleRepository
) {
    /**
     * Connects to a specific BLE device.
     */
    operator fun invoke(device: Device) {
        bleRepository.connect(device.macAddress)
    }

    /**
     * Disconnects from the current device.
     */
    fun disconnect() {
        bleRepository.disconnect()
    }
}
