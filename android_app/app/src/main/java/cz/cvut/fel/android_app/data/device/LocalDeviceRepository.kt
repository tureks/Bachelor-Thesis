package cz.cvut.fel.android_app.data.device

import cz.cvut.fel.android_app.domain.model.Device
import cz.cvut.fel.android_app.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalDeviceRepository(private val deviceDao: DeviceDao) : DeviceRepository {

    override fun getAll(): Flow<List<Device>> =
        deviceDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(device: Device): Long =
        deviceDao.insert(device.toEntity())

    override suspend fun delete(device: Device) =
        deviceDao.delete(device.toEntity())

    override suspend fun updateLastConnected(id: Int, timestamp: Long) =
        deviceDao.updateLastConnected(id, timestamp)
}

private fun DeviceEntity.toDomain() = Device(id, name, macAddress, lastConnected)
private fun Device.toEntity() = DeviceEntity(id, name, macAddress, lastConnected)