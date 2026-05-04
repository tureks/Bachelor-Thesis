package cz.cvut.fel.android_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cz.cvut.fel.android_app.App
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
    private val userRepository: UserRepository
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
        viewModelScope.launch {
            val base = _uiState.value.user ?: defaultUser()
            userRepository.saveUser(base.copy(preferredUnit = unit))
        }
    }

    fun updateMeasurementMode(isMultipoint: Boolean) {
        viewModelScope.launch {
            val base = _uiState.value.user ?: defaultUser()
            userRepository.saveUser(base.copy(multipointMeasurement = isMultipoint))
        }
    }

    fun updateProfile(firstName: String, lastName: String, email: String) {
        viewModelScope.launch {
            val base = _uiState.value.user ?: defaultUser()
            userRepository.saveUser(base.copy(firstName = firstName, lastName = lastName, email = email))
        }
    }

    fun updateSinglePointHeight(height: Double) {
        viewModelScope.launch {
            val base = _uiState.value.user ?: defaultUser()
            userRepository.saveUser(base.copy(singlePointHeight = height))
        }
    }

    private fun defaultUser() = User(
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
