package com.example.guardianlink

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import auth.OtpUiState
import contacts.ContactsPermissionState
import model.EmergencyContact
import ui.ActiveSOSScreen
import ui.AuthScreenComposable
import ui.EmergencyContactsScreen
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OtpScreenVerifyingPreview() {
    SetupOtpScreen(
        uiState = OtpUiState.Verifying,
        phone = "+91 9345771470",
        otp = "482916",
        onPhoneChange = {},
        onOtpChange = {},
        onSendOtp = {},
        onVerifyOtp = {},
        onErrorDismiss = {}
    )
}

// ── Emergency Contacts ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmergencyContactsEmptyPreview() {
    EmergencyContactsScreen(
        contacts = emptyList(),
        error = "",
        showAddDialog = false,
        deviceContacts = emptyList(),
        isLoadingContacts = false,
        permissionState = ContactsPermissionState(isGranted = false, launchRequest = {}),
        onAddContact = { _, _ -> },
        onAddDeviceContact = {},
        onRemoveContact = {},
        onToggleGPS = {},
        onToggleAudio = {},
        onShowAddDialog = {},
        onDismissError = {},
        onLoadDeviceContacts = {},
        onContinue = {},
        onBack = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmergencyContactsWithDataPreview() {
    EmergencyContactsScreen(
        contacts = listOf(
            EmergencyContact("1", "Mom", "+91 9876543210"),
            EmergencyContact("2", "Dad", "+91 8765432109", includeAudio = false),
            EmergencyContact("3", "Sister", "+91 7654321098"),
        ),
        error = "",
        showAddDialog = false,
        deviceContacts = emptyList(),
        isLoadingContacts = false,
        permissionState = ContactsPermissionState(isGranted = true, launchRequest = {}),
        onAddContact = { _, _ -> },
        onAddDeviceContact = {},
        onRemoveContact = {},
        onToggleGPS = {},
        onToggleAudio = {},
        onShowAddDialog = {},
        onDismissError = {},
        onLoadDeviceContacts = {},
        onContinue = {},
        onBack = {},
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

// ── Active SOS ──

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActiveSOSOnlinePreview() {
    ActiveSOSScreen(
        contacts = listOf(
            EmergencyContact("1", "Mom", "+91 9876543210"),
            EmergencyContact("2", "Dad", "+91 8765432109"),
            EmergencyContact("3", "Sister", "+91 7654321098"),
        ),
        isOnline = true,
        onCancel = {},
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActiveSOSOfflinePreview() {
    ActiveSOSScreen(
        contacts = listOf(
            EmergencyContact("1", "Mom", "+91 9876543210"),
            EmergencyContact("2", "Dad", "+91 8765432109"),
        ),
        isOnline = false,
        onCancel = {},
    )
}
