package cz.cvut.fel.android_app.data.device

import kotlinx.coroutines.flow.Flow

class DeviceRepository(private val deviceDao: DeviceDao) {

    fun getAll(): Flow<List<Device>> = deviceDao.getAll()

    suspend fun insert(device: Device) = deviceDao.insert(device)

    suspend fun delete(device: Device) = deviceDao.delete(device)

    suspend fun updateLastConnected(id: Int, timestamp: Long) =
        deviceDao.updateLastConnected(id, timestamp)
}