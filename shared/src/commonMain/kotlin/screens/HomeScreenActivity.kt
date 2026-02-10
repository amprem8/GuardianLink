package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ui.HomeScreen

class HomeScreenActivity : Screen {

    @Composable
    override fun Content() {
        HomeScreen(
            userName = "",
            phoneNumber = "",
            contacts = emptyList(),
            voicePhrase = "",
            gestureType = "",
            isOnline = true,
            onTriggerSOS = {},
            onEditContacts = {},
            onEditConfig = {},
            onLogout = {}
        )
    }
}

