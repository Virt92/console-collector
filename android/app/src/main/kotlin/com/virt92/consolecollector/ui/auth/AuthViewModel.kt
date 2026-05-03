package com.virt92.consolecollector.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virt92.consolecollector.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthUiState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Email and password are required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { container.repository.login(email, password) }
                .onSuccess { _state.update { AuthUiState(success = true) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.toUiMessage()) } }
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        city: String?,
        country: String?,
    ) {
        if (email.isBlank() || password.length < 6 || displayName.isBlank()) {
            _state.update { it.copy(error = "Email, password (6+ chars) and display name are required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                container.repository.register(
                    email = email,
                    password = password,
                    displayName = displayName,
                    city = city,
                    country = country,
                )
            }
                .onSuccess { _state.update { AuthUiState(success = true) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.toUiMessage()) } }
        }
    }
}

internal fun Throwable.toUiMessage(): String = when (this) {
    is HttpException -> "Server returned ${code()}: ${message()}"
    else -> message ?: "Unknown error"
}
