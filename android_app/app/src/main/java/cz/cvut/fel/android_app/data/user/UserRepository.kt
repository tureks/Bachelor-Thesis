package cz.cvut.fel.android_app.data.user

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val user: Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun clearUser()
}