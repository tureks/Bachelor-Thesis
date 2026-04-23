package cz.cvut.fel.android_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cz.cvut.fel.android_app.data.device.DeviceEntity
import cz.cvut.fel.android_app.data.device.DeviceDao
import cz.cvut.fel.android_app.data.measurement.StreamMeasurementEntity
import cz.cvut.fel.android_app.data.measurement.StreamMeasurementDao
import cz.cvut.fel.android_app.data.stream_segment.StreamSegmentEntity
import cz.cvut.fel.android_app.data.stream_segment.StreamSegmentDao
import cz.cvut.fel.android_app.data.velocity_point.VelocityPointEntity
import cz.cvut.fel.android_app.data.velocity_point.VelocityPointDao

@Database(
    entities = [
        DeviceEntity::class,
        StreamMeasurementEntity::class,
        StreamSegmentEntity::class,
        VelocityPointEntity::class
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