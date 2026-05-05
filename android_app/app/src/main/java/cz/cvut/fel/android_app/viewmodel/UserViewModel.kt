package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.UserProfile
import cz.cvut.fel.android_app.domain.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true
)

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        userRepository.userProfile
            .onEach { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun updatePreferredUnit(unit: MeasurementUnit) {
        viewModelScope.launch {
            val base = _uiState.value.profile ?: defaultProfile()
            userRepository.saveUserProfile(base.copy(preferredUnit = unit))
        }
    }

    fun updateMeasurementMode(isMultipoint: Boolean) {
        viewModelScope.launch {
            val base = _uiState.value.profile ?: defaultProfile()
            userRepository.saveUserProfile(base.copy(multipointMeasurement = isMultipoint))
        }
    }

    fun updateProfile(firstName: String, lastName: String, email: String) {
        viewModelScope.launch {
            val base = _uiState.value.profile ?: defaultProfile()
            userRepository.saveUserProfile(base.copy(firstName = firstName, lastName = lastName, email = email))
        }
    }

    fun updateSinglePointHeight(height: Double) {
        viewModelScope.launch {
            val base = _uiState.value.profile ?: defaultProfile()
            userRepository.saveUserProfile(base.copy(singlePointHeight = height))
        }
    }

    private fun defaultProfile() = UserProfile(
        firstName = "",
        lastName = "",
        email = "",
        multipointMeasurement = true,
        singlePointHeight = 0.6,
        preferredUnit = MeasurementUnit.HYDROMETRIC
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as App
                UserViewModel(userRepository = app.userRepository)
            }
        }
    }
}
