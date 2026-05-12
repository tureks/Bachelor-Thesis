package cz.cvut.fel.android_app.domain.repository

import cz.cvut.fel.android_app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** Emits the current user profile, or null if no profile has been saved yet. */
    val userProfile: Flow<UserProfile?>
    /** Persists [profile] to DataStore, replacing any existing profile. */
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun clearUserProfile()
}