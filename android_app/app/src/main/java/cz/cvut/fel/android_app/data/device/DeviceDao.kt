package cz.cvut.fel.android_app.data.device

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT id, name, mac_address, last_connected FROM device ORDER BY last_connected DESC")
    fun getAll(): Flow<List<Device>>

    @Insert
    suspend fun insert(device: Device): Long

    @Delete
    suspend fun delete(device: Device)

    @Query("UPDATE device SET last_connected = :timestamp WHERE id = :id")
    suspend fun updateLastConnected(id: Int, timestamp: Long)
}