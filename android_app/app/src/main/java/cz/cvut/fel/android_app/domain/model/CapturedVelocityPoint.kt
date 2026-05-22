package cz.cvut.fel.android_app.domain.model

/** Reading converted to [VelocityPoint] on segment completion. */
data class CapturedVelocityPoint(
    val velocity: Double,
    val measureHeight: Double?
)