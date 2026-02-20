package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import session.UserSession
import ui.HomeScreen
import ui.HomeUiState
import ui.HomeActions

class HomeScreenActivity : Screen {

    @Composable
    override fun Content() {
        val state = HomeUiState(
            userName = UserSession.userName,
            phoneNumber = "",
            contacts = emptyList(),
            voicePhrase = "",
            gestureType = "",
            isOnline = true
        )

        val actions = HomeActions(
            onTriggerSOS = {},
            onEditContacts = {},
            onEditConfig = {},
            onLogout = {}
        )

        HomeScreen(
            state = state,
            actions = actions
        )
    }
}
