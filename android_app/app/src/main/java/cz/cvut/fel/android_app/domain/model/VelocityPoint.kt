package cz.cvut.fel.android_app.domain.model

data class VelocityPoint(
    val id: Int = 0,
    val segmentId: Int,
    val velocity: Double,
    val measureHeight: Double?
)