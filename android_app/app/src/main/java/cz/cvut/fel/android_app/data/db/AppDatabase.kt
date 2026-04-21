package cz.cvut.fel.android_app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cz.cvut.fel.android_app.data.device.Device
import cz.cvut.fel.android_app.data.device.DeviceDao
import cz.cvut.fel.android_app.data.measurement.StreamMeasurement
import cz.cvut.fel.android_app.data.measurement.StreamMeasurementDao
import cz.cvut.fel.android_app.data.stream_segment.StreamSegment
import cz.cvut.fel.android_app.data.stream_segment.StreamSegmentDao
import cz.cvut.fel.android_app.data.velocity_point.VelocityPoint
import cz.cvut.fel.android_app.data.velocity_point.VelocityPointDao

@Database(
    entities = [
        Device::class,
        StreamMeasurement::class,
        StreamSegment::class,
        VelocityPoint::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun measurementDao(): StreamMeasurementDao
    abstract fun streamSegmentDao(): StreamSegmentDao
    abstract fun velocityPointDao(): VelocityPointDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                    .build()
                    .also { INSTANCE = it }
            }
    }
}