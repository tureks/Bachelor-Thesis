package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val userProfile: Flow<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun clearUserProfile()
}