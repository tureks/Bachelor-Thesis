package cz.cvut.fel.android_app.data.user

import kotlinx.coroutines.flow.Flow

class UserRepository(private val dataSource: UserDataSource) {

    val user: Flow<User?> = dataSource.user

    suspend fun saveUser(user: User) = dataSource.saveUser(user)

    suspend fun clearUser() = dataSource.clearUser()
}