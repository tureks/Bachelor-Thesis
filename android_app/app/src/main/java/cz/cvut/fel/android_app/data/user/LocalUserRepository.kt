package cz.cvut.fel.android_app.data.user

import cz.cvut.fel.android_app.domain.model.UserProfile
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class LocalUserRepository(private val dataSource: UserDataSource) : UserRepository {

    override val userProfile: Flow<UserProfile?> = dataSource.userProfile

    override suspend fun saveUserProfile(profile: UserProfile) = dataSource.saveUserProfile(profile)

    override suspend fun clearUserProfile() = dataSource.clearUserProfile()
}