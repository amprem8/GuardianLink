package screenmodel

import api.AuthApi
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.AuthResponse
import models.SignupRequest
import session.UserSession
import util.ApiException

sealed class SignupUiState {
    object Idle : SignupUiState()
    object Loading : SignupUiState()
    data class Success(val response: AuthResponse) : SignupUiState()
    data class Error(val message: String) : SignupUiState()
}

class SignupScreenModel : ScreenModel {

    private val authApi = AuthApi()

    private val _uiState = MutableStateFlow<SignupUiState>(SignupUiState.Idle)
    val uiState: StateFlow<SignupUiState> = _uiState

    /**
     * Client-side validation + API call.
     * On 201 success, state becomes [SignupUiState.Success].
     * On any error (400/409/network), state becomes [SignupUiState.Error].
     */
    fun signup(name: String, email: String, password: String, confirmPassword: String, agreeToTerms: Boolean) {
        // ── Client-side quick checks ──
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = SignupUiState.Error("Please fill in all fields")
            return
        }
        if (!email.contains("@")) {
            _uiState.value = SignupUiState.Error("Please enter a valid email")
            return
        }
        if (password.length < 8) {
            _uiState.value = SignupUiState.Error("Password must be at least 8 characters")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = SignupUiState.Error("Passwords do not match")
            return
        }
        if (!agreeToTerms) {
            _uiState.value = SignupUiState.Error("Please accept the terms and conditions")
            return
        }

        // ── API call ──
        _uiState.value = SignupUiState.Loading

        screenModelScope.launch {
            val result = authApi.signup(
                SignupRequest(
                    email = email.trim().lowercase(),
                    password = password,
                    name = name.trim()
                )
            )

            result
                .onSuccess { response ->
                    UserSession.login(response)
                    _uiState.value = SignupUiState.Success(response)
                }
                .onFailure { throwable ->
                    val msg = if (throwable is ApiException) {
                        throwable.apiError.message
                    } else {
                        throwable.message ?: "Something went wrong"
                    }
                    _uiState.value = SignupUiState.Error(msg)
                }
        }
    }

    fun dismissError() {
        _uiState.value = SignupUiState.Idle
    }
}
