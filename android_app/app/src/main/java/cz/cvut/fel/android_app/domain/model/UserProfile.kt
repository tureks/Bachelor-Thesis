package cz.cvut.fel.android_app.domain.model

/**
 * @property multipointMeasurement true = user adds reading depth manually; false = universal point at [singlePointHeight]
 * @property singlePointHeight probe depth as a 0.0–1.0 fraction, shown as percentage
 * @property developerMode enables measurement without connected hardware
 */
data class UserProfile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val multipointMeasurement: Boolean,
    val singlePointHeight: Double,
    val preferredUnit: MeasurementUnit = MeasurementUnit.HYDROMETRIC,
    val developerMode: Boolean = false
)
