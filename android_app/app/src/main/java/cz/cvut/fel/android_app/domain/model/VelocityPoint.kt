package cz.cvut.fel.android_app.domain.model

/**
 * @property velocity m/s
 * @property measureHeight probe position as % of total depth (0–100)
 */
data class VelocityPoint(
    val id: Int = 0,
    val segmentId: Int,
    val velocity: Double,
    val measureHeight: Double?
)