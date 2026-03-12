package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import session.UserSession
import storage.AppStorage
import ui.ProfileActions
import ui.ProfileScreen
import ui.ProfileUiState

class ProfileScreenActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        val userName    = UserSession.userName.ifEmpty { AppStorage.getUserName() }
        val userEmail   = UserSession.userEmail.ifEmpty { AppStorage.getUserEmail() }
        val phoneNumber = AppStorage.getPhoneNumber()

        // Use mutable states so toggling reflects immediately without re-entering
        var safePin              by remember { mutableStateOf(AppStorage.getSafePin()) }
        var continuousMonitoring by remember { mutableStateOf(AppStorage.isContinuousMonitoring()) }
        var voiceChoice          by remember { mutableStateOf(AppStorage.isVoiceChoice()) }

        val state = ProfileUiState(
            userName             = userName,
            userEmail            = userEmail,
            phoneNumber          = phoneNumber,
            safePin              = safePin,
            continuousMonitoring = continuousMonitoring,
            voiceChoice          = voiceChoice,
        )

        val actions = ProfileActions(
            onSavePin = { pin ->
                AppStorage.setSafePin(pin)
                safePin = pin
            },
            onSetContinuousMonitoring = { enabled ->
                AppStorage.setContinuousMonitoring(enabled)
                continuousMonitoring = enabled
            },
            onSetVoiceChoice = { enabled ->
                AppStorage.setVoiceChoice(enabled)
                voiceChoice = enabled
            },
            onBack = { navigator?.pop() },
        )

        ProfileScreen(state = state, actions = actions)
    }
}

