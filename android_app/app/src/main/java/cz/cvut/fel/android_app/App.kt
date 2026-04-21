package cz.cvut.fel.android_app

import android.app.Application
import cz.cvut.fel.android_app.data.AppDatabase
import cz.cvut.fel.android_app.data.device.DeviceRepository
import cz.cvut.fel.android_app.data.measurement.StreamMeasurementRepository
import cz.cvut.fel.android_app.data.user.UserDataSource
import cz.cvut.fel.android_app.data.user.UserRepository

class App : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val userRepository by lazy { UserRepository(UserDataSource(this)) }

    val deviceRepository by lazy { DeviceRepository(database.deviceDao()) }

    val measurementRepository by lazy {
        StreamMeasurementRepository(
            database.measurementDao(),
            database.streamSegmentDao(),
            database.velocityPointDao()
        )
    }
}