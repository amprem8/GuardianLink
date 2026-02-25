package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import network.NetworkConnectivityObserver
import storage.ContactStorage
import ui.ActiveSOSScreen

class ActiveSOSActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        // Real-time network connectivity state
        val isOnline by NetworkConnectivityObserver.isOnline.collectAsState()

        // Load saved emergency contacts
        val savedContacts = ContactStorage.loadContacts()

        ActiveSOSScreen(
            contacts = savedContacts,
            isOnline = isOnline,
            onCancel = { navigator?.pop() },
        )
    }
}
