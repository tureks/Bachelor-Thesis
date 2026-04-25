package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow

class ObserveBleConnectionStateUseCase(
    private val bleRepository: BleRepository
) {
    /**
     * Returns a stream of the current Bluetooth connection status.
     * Use this to show "Connected/Disconnected" in the UI.
     */
    operator fun invoke(): Flow<BleConnectionState> = bleRepository.connectionState
}
