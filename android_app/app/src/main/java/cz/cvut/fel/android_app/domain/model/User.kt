package cz.cvut.fel.android_app.domain.model

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val multipointMeasurement: Boolean,
    val singlePointHeight: Double
)