package auth

sealed class OtpUiState {
    object PhoneEntry : OtpUiState()
    object OtpEntry : OtpUiState()
    object Loading : OtpUiState()
    data class Error(val message: String) : OtpUiState()
    object Success : OtpUiState()
}
