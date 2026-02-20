package auth

import api.AuthApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import util.ApiException

class OtpLogic(private val authApi: AuthApi) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState.PhoneEntry)
    val uiState: StateFlow<OtpUiState> = _uiState

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp

    fun onPhoneChanged(value: String) {
        _phone.value=value
    }

    fun onOtpChanged(value: String) {
        _otp.value=value
    }
    private fun normalizePhone(raw: String): String {
        val digits = raw.replace("\\D".toRegex(), "")
        return "+91$digits"
    }

    fun sendOtp() {
        val normalized = normalizePhone(phone.value)

        if (!normalized.matches(Regex("^\\+91[6-9]\\d{9}$"))) {
            _uiState.value = OtpUiState.Error("Enter a valid Indian mobile number")
            return
        }

        _uiState.value = OtpUiState.Loading

        scope.launch {
            authApi.sendOtp(normalized)
                .onSuccess { _uiState.value = OtpUiState.OtpEntry }
                .onFailure { _uiState.value = OtpUiState.Error(it.message?:"Unknown") }
        }
    }

    fun verifyOtp() {
        _uiState.value = OtpUiState.Verifying

        scope.launch {
            authApi.verifyOtp(normalizePhone(phone.value), otp.value)
                .onSuccess { _uiState.value = OtpUiState.Success }
                .onFailure { _uiState.value = OtpUiState.Error(extract(it)) }
        }
    }

    fun dismissError() {
        _uiState.value = OtpUiState.PhoneEntry
    }

    private fun extract(t: Throwable): String =
        if (t is ApiException) t.apiError.message else "Something went wrong"
}
