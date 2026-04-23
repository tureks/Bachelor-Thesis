package cz.cvut.fel.android_app.domain.model

data class Device(
    val id: Int = 0,
    val name: String,
    val macAddress: String,
    val lastConnected: Long?
)