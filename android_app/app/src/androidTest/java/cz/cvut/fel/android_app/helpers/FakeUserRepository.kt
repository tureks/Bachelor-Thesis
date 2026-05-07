package cz.cvut.fel.android_app.helpers

import cz.cvut.fel.android_app.domain.model.UserProfile
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRepository(profile: UserProfile? = null) : UserRepository {

    private val _profile = MutableStateFlow(profile)

    override val userProfile: Flow<UserProfile?> = _profile

    override suspend fun saveUserProfile(profile: UserProfile) {
        _profile.value = profile
    }

    override suspend fun clearUserProfile() {
        _profile.value = null
    }
}