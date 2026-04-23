package cz.cvut.fel.android_app.data.user

import cz.cvut.fel.android_app.domain.model.User
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class LocalUserRepository(private val dataSource: UserDataSource) : UserRepository {

    override val user: Flow<User?> = dataSource.user

    override suspend fun saveUser(user: User) = dataSource.saveUser(user)

    override suspend fun clearUser() = dataSource.clearUser()
}