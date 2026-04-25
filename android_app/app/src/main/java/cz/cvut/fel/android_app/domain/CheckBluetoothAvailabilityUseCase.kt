package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.repository.BleRepository

class CheckBluetoothAvailabilityUseCase(
    private val bleRepository: BleRepository
) {
    /**
     * Checks if Bluetooth is currently enabled on the system.
     * Use this before starting a scan or connection.
     */
    fun isEnabled(): Boolean = bleRepository.isBluetoothEnabled()
}
