package auth

sealed class OtpUiState {
    object PhoneEntry : OtpUiState()
    object OtpEntry : OtpUiState()
    object Loading : OtpUiState()
    object Verifying : OtpUiState()           // Spinner while verify API runs
    data class Error(val message: String) : OtpUiState()
    object Success : OtpUiState()
}
