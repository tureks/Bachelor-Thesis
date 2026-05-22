package cz.cvut.fel.android_app

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import com.google.android.gms.location.LocationServices
import cz.cvut.fel.android_app.data.AppDatabase
import cz.cvut.fel.android_app.data.bluetooth.LocalBleRepository
import cz.cvut.fel.android_app.data.device.LocalDeviceRepository
import cz.cvut.fel.android_app.data.location.AndroidLocationRepository
import cz.cvut.fel.android_app.data.measurement.LocalStreamMeasurementRepository
import cz.cvut.fel.android_app.data.user.LocalUserRepository
import cz.cvut.fel.android_app.data.user.UserDataSource
import cz.cvut.fel.android_app.domain.repository.BleRepository
import cz.cvut.fel.android_app.domain.repository.DeviceRepository
import cz.cvut.fel.android_app.domain.repository.LocationRepository
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.UserRepository

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

    val bleRepository: BleRepository by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        LocalBleRepository(this, bluetoothManager.adapter, deviceRepository)
    }

    val locationRepository: LocationRepository by lazy {
        AndroidLocationRepository(this, LocationServices.getFusedLocationProviderClient(this))
    }
}
