package cz.cvut.fel.android_app.domain

import cz.cvut.fel.android_app.domain.model.User
import cz.cvut.fel.android_app.domain.repository.UserRepository

class UpdateUserProfileUseCase(
    private val userRepository: UserRepository
) {
    /**
     * Updates the user profile. 
     * singlePointHeight is a ratio (0.0 to 1.0) and doesn't require unit conversion.
     */
    suspend operator fun invoke(user: User) {
        userRepository.saveUser(user)
    }
}
