package com.virt92.consolecollector.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virt92.consolecollector.data.model.UpdateProfileRequest
import com.virt92.consolecollector.data.model.UserDto
import com.virt92.consolecollector.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserDto? = null,
    val saving: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val shareUrl: String? = null,
)

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            runCatching { container.repository.me() }
                .onSuccess { user -> _state.update { it.copy(user = user) } }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun save(displayName: String, city: String, country: String, bio: String) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, message = null, error = null) }
            runCatching {
                container.repository.updateProfile(
                    UpdateProfileRequest(
                        displayName = displayName.takeIf { it.isNotBlank() },
                        city = city.takeIf { it.isNotBlank() },
                        country = country.takeIf { it.isNotBlank() },
                        bio = bio.takeIf { it.isNotBlank() },
                    ),
                )
            }
                .onSuccess { user -> _state.update { it.copy(saving = false, user = user, message = "Saved!") } }
                .onFailure { e -> _state.update { it.copy(saving = false, error = e.message) } }
        }
    }

    fun shareCollection() {
        viewModelScope.launch {
            runCatching { container.repository.shareCollection() }
                .onSuccess { _state.update { st -> st.copy(shareUrl = it.url) } }
                .onFailure { e -> _state.update { st -> st.copy(error = e.message) } }
        }
    }

    fun logout() {
        viewModelScope.launch { container.repository.logout() }
    }
}
