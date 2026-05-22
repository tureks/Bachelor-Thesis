package cz.cvut.fel.android_app.helpers

import android.content.Context
import androidx.room.Room
import cz.cvut.fel.android_app.data.AppDatabase
import cz.cvut.fel.android_app.data.measurement.LocalStreamMeasurementRepository
import cz.cvut.fel.android_app.domain.repository.StreamMeasurementRepository

object TestDb {

    fun build(context: Context): AppDatabase =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    fun repository(db: AppDatabase): StreamMeasurementRepository =
        LocalStreamMeasurementRepository(
            measurementDao = db.measurementDao(),
            segmentDao = db.streamSegmentDao(),
            velocityPointDao = db.velocityPointDao(),
        )
}