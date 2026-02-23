package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import session.UserSession
import storage.AppStorage
import storage.ContactStorage
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

        // Load saved emergency contacts for display
        val savedContacts = ContactStorage.loadContacts()

        val state = HomeUiState(
            userName = userName,
            phoneNumber = phoneNumber,
            contacts = savedContacts.map { it.name },
            voicePhrase = "",
            gestureType = "",
            isOnline = true
        )

        val actions = HomeActions(
            onTriggerSOS = {},
            onEditContacts = { navigator?.push(EmergencyContactsActivity()) },
            onEditConfig = {},
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
