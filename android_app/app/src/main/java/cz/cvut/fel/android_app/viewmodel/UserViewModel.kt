package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.UpdateUserProfileUseCase
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.User
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserUiState(
    val user: User? = null,
    val isLoading: Boolean = true
)

class UserViewModel(
    private val userRepository: UserRepository,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        userRepository.user
            .onEach { user ->
                _uiState.update { it.copy(user = user, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun updatePreferredUnit(unit: MeasurementUnit) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            updateUserProfileUseCase(currentUser.copy(preferredUnit = unit))
        }
    }

    fun updateMeasurementMode(isMultipoint: Boolean) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            updateUserProfileUseCase(currentUser.copy(multipointMeasurement = isMultipoint))
        }
    }

    fun updateProfile(firstName: String, lastName: String, email: String) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            updateUserProfileUseCase(
                currentUser.copy(
                    firstName = firstName,
                    lastName = lastName,
                    email = email
                )
            )
        }
    }

    fun updateSinglePointHeight(height: Double) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            updateUserProfileUseCase(currentUser.copy(singlePointHeight = height))
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                UserViewModel(
                    userRepository = app.userRepository,
                    updateUserProfileUseCase = UpdateUserProfileUseCase(app.userRepository)
                )
            }
        }
    }
}
