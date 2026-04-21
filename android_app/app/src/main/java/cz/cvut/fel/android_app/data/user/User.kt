package cz.cvut.fel.android_app.data.user

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val multipointMeasurement: Boolean,
    val singlePointHeight: Double
)