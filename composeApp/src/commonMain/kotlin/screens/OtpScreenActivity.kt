package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import api.AuthApi
import auth.OtpLogic
import auth.OtpUiState
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import session.UserSession
import storage.AppStorage
import ui.SetupOtpScreen

class OtpScreenActivity : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val logic = remember { OtpLogic(authApi = AuthApi()) }
        val uiState by logic.uiState.collectAsState()
        val phone by logic.phone.collectAsState()
        val otp by logic.otp.collectAsState()

        // Navigate to Home only when OTP API returns success (200)
        LaunchedEffect(uiState) {
            if (uiState is OtpUiState.Success) {
                // Persist registration FIRST (synchronous commit on Android)
                // so even an immediate process kill won't lose the flag
                val name  = UserSession.userName.ifEmpty { "User" }
                val email = UserSession.userEmail
                AppStorage.markRegistered(
                    userName    = name,
                    userEmail   = email,
                    phoneNumber = phone,
                )
                navigator?.replace(EmergencyContactsActivity())
            }
        }

        SetupOtpScreen(
            uiState = uiState,
            phone = phone,
            otp = otp,
            onPhoneChange = logic::onPhoneChanged,
            onOtpChange = logic::onOtpChanged,
            onSendOtp = logic::sendOtp,
            onVerifyOtp = logic::verifyOtp,
            onErrorDismiss = logic::dismissError
        )
    }
}
