package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val user: Flow<User?>
    suspend fun saveUser(user: User)
    suspend fun clearUser()
}