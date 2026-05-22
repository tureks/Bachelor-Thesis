package cz.cvut.fel.android_app.data.device

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device")
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "mac_address") val macAddress: String,
    @ColumnInfo(name = "last_connected") val lastConnected: Long?
)