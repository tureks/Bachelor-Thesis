package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getAll(): Flow<List<Device>>
    suspend fun insert(device: Device): Long
    suspend fun delete(device: Device)
    suspend fun updateLastConnected(id: Int, timestamp: Long)
}
