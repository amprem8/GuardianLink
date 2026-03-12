package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ui.HomeActions
import ui.HomeScreen
import ui.HomeUiState
import ui.PermissionsUiState

class HomeScreenActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        val userName    = UserSession.userName.ifEmpty { AppStorage.getUserName() }
        val phoneNumber = AppStorage.getPhoneNumber()

        val savedContacts  = ContactStorage.loadContacts()
        val triggerConfig  = TriggerConfigStorage.loadConfig()
        val isOnline by NetworkConnectivityObserver.isOnline.collectAsState()

        // Monitoring toggles — mutable so UI reacts immediately without recompose from storage
        var continuousMonitoring by remember { mutableStateOf(AppStorage.isContinuousMonitoring()) }
        var voiceChoice          by remember { mutableStateOf(AppStorage.isVoiceChoice()) }

        val locationPerm     = rememberLocationPermission()
        val microphonePerm   = rememberMicrophonePermission()
        val phoneCallPerm    = rememberPhoneCallPermission()
        val smsPerm          = rememberSmsPermission()
        val notificationPerm = rememberNotificationPermission()

        val permissionsState = PermissionsUiState(
            location      = locationPerm,
            microphone    = microphonePerm,
            phoneCall     = phoneCallPerm,
            sms           = smsPerm,
            notifications = notificationPerm,
        )

        val state = HomeUiState(
            userName             = userName,
            phoneNumber          = phoneNumber,
            contacts             = savedContacts.map { it.name },
            voicePhrase          = triggerConfig.voicePhrase,
            gestureType          = triggerConfig.gestureType,
            isOnline             = isOnline,
            continuousMonitoring = continuousMonitoring,
            voiceChoice          = voiceChoice,
            permissions          = permissionsState,
        )

        val actions = HomeActions(
            onTriggerSOS  = { navigator?.push(ActiveSOSActivity()) },
            onEditContacts = { navigator?.push(EmergencyContactsActivity(isSetupFlow = false)) },
            onEditConfig  = { navigator?.push(TriggerConfigActivity()) },
            onProfileClick = { navigator?.push(ProfileScreenActivity()) },
            onLogout = {
                AppStorage.logout()
                UserSession.logout()
                navigator?.replaceAll(LoginScreenActivity())
            },
            onSetContinuousMonitoring = { enabled ->
                AppStorage.setContinuousMonitoring(enabled)
                continuousMonitoring = enabled
            },
            onSetVoiceChoice = { enabled ->
                AppStorage.setVoiceChoice(enabled)
                voiceChoice = enabled
            },
        )

        HomeScreen(state = state, actions = actions)
    }
}
