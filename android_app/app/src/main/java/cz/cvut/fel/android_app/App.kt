package cz.cvut.fel.android_app

import android.app.Application
import cz.cvut.fel.android_app.data.AppDatabase
import cz.cvut.fel.android_app.data.device.DeviceRepository
import cz.cvut.fel.android_app.data.device.LocalDeviceRepository
import cz.cvut.fel.android_app.data.measurement.LocalStreamMeasurementRepository
import cz.cvut.fel.android_app.data.measurement.StreamMeasurementRepository
import cz.cvut.fel.android_app.data.user.LocalUserRepository
import cz.cvut.fel.android_app.data.user.UserDataSource
import cz.cvut.fel.android_app.data.user.UserRepository

class App : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val userRepository: UserRepository by lazy { LocalUserRepository(UserDataSource(this)) }

    val deviceRepository: DeviceRepository by lazy { LocalDeviceRepository(database.deviceDao()) }

    val measurementRepository: StreamMeasurementRepository by lazy {
        LocalStreamMeasurementRepository(
            database.measurementDao(),
            database.streamSegmentDao(),
            database.velocityPointDao()
        )
    }
}