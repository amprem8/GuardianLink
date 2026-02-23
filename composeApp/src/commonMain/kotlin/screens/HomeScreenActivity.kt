package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import session.UserSession
import storage.AppStorage
import ui.HomeScreen
import ui.HomeUiState
import ui.HomeActions

class HomeScreenActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        // Use in-memory session if available, otherwise fall back to persisted data
        val userName = UserSession.userName.ifEmpty { AppStorage.getUserName() }
        val userEmail = UserSession.userEmail.ifEmpty { AppStorage.getUserEmail() }
        val phoneNumber = AppStorage.getPhoneNumber()

        val state = HomeUiState(
            userName = userName,
            phoneNumber = phoneNumber,
            contacts = emptyList(),
            voicePhrase = "",
            gestureType = "",
            isOnline = true
        )

        val actions = HomeActions(
            onTriggerSOS = {},
            onEditContacts = {},
            onEditConfig = {},
            onLogout = {
                // Clear persisted registration + in-memory session
                AppStorage.clear()
                UserSession.logout()
                navigator?.replaceAll(AuthScreenActivity())
            }
        )

        HomeScreen(
            state = state,
            actions = actions
        )
    }
}
