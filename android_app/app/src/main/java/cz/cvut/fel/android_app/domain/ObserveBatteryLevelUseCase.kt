package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.repository.BleRepository
import kotlinx.coroutines.flow.Flow

class ObserveBatteryLevelUseCase(
    private val bleRepository: BleRepository
) {
    /**
     * Returns a stream of the current battery level percentage (0-100)
     * of the connected flow meter.
     */
    operator fun invoke(): Flow<Int> {
        return bleRepository.batteryLevel
    }
}
