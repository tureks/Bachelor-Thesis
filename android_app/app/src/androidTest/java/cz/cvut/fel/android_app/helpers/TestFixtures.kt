package cz.cvut.fel.android_app.helpers

import cz.cvut.fel.android_app.domain.model.CapturedVelocityPoint
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.UserProfile

fun capturedPoint(velocity: Double, measureHeight: Double? = null) =
    CapturedVelocityPoint(velocity = velocity, measureHeight = measureHeight)

fun defaultUserProfile(unit: MeasurementUnit = MeasurementUnit.HYDROMETRIC) = UserProfile(
    firstName = "Jan",
    lastName = "Novák",
    email = "jan@example.com",
    multipointMeasurement = true,
    singlePointHeight = 0.6,
    preferredUnit = unit,
)