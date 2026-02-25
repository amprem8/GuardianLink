package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import network.NetworkConnectivityObserver
import permissions.rememberLocationPermission
import permissions.rememberMicrophonePermission
import permissions.rememberNotificationPermission
import permissions.rememberPhoneCallPermission
import permissions.rememberSmsPermission
import session.UserSession
import storage.AppStorage
import storage.ContactStorage
import storage.TriggerConfigStorage
import ui.HomeScreen
import ui.HomeUiState
import ui.HomeActions
import ui.PermissionsUiState

class HomeScreenActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        // Use in-memory session if available, otherwise fall back to persisted data
        val userName = UserSession.userName.ifEmpty { AppStorage.getUserName() }
        val userEmail = UserSession.userEmail.ifEmpty { AppStorage.getUserEmail() }
        val phoneNumber = AppStorage.getPhoneNumber()

        // Load saved emergency contacts for display
        val savedContacts = ContactStorage.loadContacts()

        // Load trigger configuration
        val triggerConfig = TriggerConfigStorage.loadConfig()

        // Real-time network connectivity state
        val isOnline by NetworkConnectivityObserver.isOnline.collectAsState()

        // Runtime permissions
        val locationPerm = rememberLocationPermission()
        val microphonePerm = rememberMicrophonePermission()
        val phoneCallPerm = rememberPhoneCallPermission()
        val smsPerm = rememberSmsPermission()
        val notificationPerm = rememberNotificationPermission()

        val permissionsState = PermissionsUiState(
            location = locationPerm,
            microphone = microphonePerm,
            phoneCall = phoneCallPerm,
            sms = smsPerm,
            notifications = notificationPerm,
        )

        val state = HomeUiState(
            userName = userName,
            phoneNumber = phoneNumber,
            contacts = savedContacts.map { it.name },
            voicePhrase = triggerConfig.voicePhrase,
            gestureType = triggerConfig.gestureType,
            isOnline = isOnline,
            permissions = permissionsState,
        )

        val actions = HomeActions(
            onTriggerSOS = { navigator?.push(ActiveSOSActivity()) },
            onEditContacts = { navigator?.push(EmergencyContactsActivity(isSetupFlow = false)) },
            onEditConfig = { navigator?.push(TriggerConfigActivity()) },
            onLogout = {
                // Clear login session but keep registration data
                AppStorage.logout()
                UserSession.logout()
                navigator?.replaceAll(LoginScreenActivity())
            }
        )

        HomeScreen(
            state = state,
            actions = actions
        )
    }
}
