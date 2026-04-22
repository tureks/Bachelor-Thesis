package cz.cvut.fel.android_app.data.device

import kotlinx.coroutines.flow.Flow

class LocalDeviceRepository(private val deviceDao: DeviceDao) : DeviceRepository {

    override fun getAll(): Flow<List<Device>> = deviceDao.getAll()

    override suspend fun insert(device: Device) = deviceDao.insert(device)

    override suspend fun delete(device: Device) = deviceDao.delete(device)

    override suspend fun updateLastConnected(id: Int, timestamp: Long) =
        deviceDao.updateLastConnected(id, timestamp)
}