package screenmodel

import api.AuthApi
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import models.LoginRequest
import session.UserSession
import util.ApiException

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginScreenModel : ScreenModel {

    private val authApi = AuthApi()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please fill in all fields")
            return
        }
        if (!email.contains("@")) {
            _uiState.value = LoginUiState.Error("Please enter a valid email")
            return
        }

        _uiState.value = LoginUiState.Loading

        screenModelScope.launch {
            authApi.login(
                LoginRequest(
                    email = email.trim().lowercase(),
                    password = password
                )
            )
                .onSuccess { response ->
                    UserSession.login(response)
                    _uiState.value = LoginUiState.Success
                }
                .onFailure { throwable ->
                    val msg = if (throwable is ApiException) {
                        throwable.apiError.message
                    } else {
                        "Unable to connect. Please check your internet connection."
                    }
                    _uiState.value = LoginUiState.Error(msg)
                }
        }
    }

    fun dismissError() {
        _uiState.value = LoginUiState.Idle
    }
}
