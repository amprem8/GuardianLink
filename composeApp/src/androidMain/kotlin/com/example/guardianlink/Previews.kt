package com.example.guardianlink

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import auth.OtpUiState
import ui.AuthScreenComposable
import ui.HomeActions
import ui.HomeScreen
import ui.HomeUiState
import ui.SetupOtpScreen
import ui.SignupScreen
import ui.SplashScreenContent

// ── Splash ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreenContent()
}

// ── Auth ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthScreenPreview() {
    AuthScreenComposable(
        onLogin = {},
        onSignup = {}
    )
}

// ── Signup ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignupScreenPreview() {
    SignupScreen(
        isLoading = false,
        error = "",
        onSignup = { _, _, _, _, _ -> },
        onDismissError = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignupScreenLoadingPreview() {
    SignupScreen(
        isLoading = true,
        error = "",
        onSignup = { _, _, _, _, _ -> },
        onDismissError = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignupScreenErrorPreview() {
    SignupScreen(
        isLoading = false,
        error = "Email already registered",
        onSignup = { _, _, _, _, _ -> },
        onDismissError = {}
    )
}

// ── OTP ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OtpScreenPhoneEntryPreview() {
    SetupOtpScreen(
        uiState = OtpUiState.PhoneEntry,
        phone = "",
        otp = "",
        onPhoneChange = {},
        onOtpChange = {},
        onSendOtp = {},
        onVerifyOtp = {},
        onErrorDismiss = {}
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OtpScreenOtpEntryPreview() {
    SetupOtpScreen(
        uiState = OtpUiState.OtpEntry,
        phone = "+1 555-123-4567",
        otp = "123",
        onPhoneChange = {},
        onOtpChange = {},
        onSendOtp = {},
        onVerifyOtp = {},
        onErrorDismiss = {}
    )
}

// ── Home ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        state = HomeUiState(
            userName = "John Doe",
            phoneNumber = "+1 555-123-4567",
            contacts = listOf("Mom", "Dad", "911"),
            voicePhrase = "Help me",
            gestureType = "double-tap",
            isOnline = true
        ),
        actions = HomeActions(
            onTriggerSOS = {},
            onEditContacts = {},
            onEditConfig = {},
            onLogout = {}
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenOfflinePreview() {
    HomeScreen(
        state = HomeUiState(
            userName = "John Doe",
            phoneNumber = "+1 555-123-4567",
            contacts = emptyList(),
            voicePhrase = "",
            gestureType = "shake",
            isOnline = false
        ),
        actions = HomeActions(
            onTriggerSOS = {},
            onEditContacts = {},
            onEditConfig = {},
            onLogout = {}
        )
    )
}
