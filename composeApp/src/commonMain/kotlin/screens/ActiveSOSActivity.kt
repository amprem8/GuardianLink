package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import network.NetworkConnectivityObserver
import storage.AppStorage
import storage.ContactStorage
import ui.ActiveSOSScreen

class ActiveSOSActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        val isOnline by NetworkConnectivityObserver.isOnline.collectAsState()
        val savedContacts = ContactStorage.loadContacts()
        val safePin = AppStorage.getSafePin()

        ActiveSOSScreen(
            contacts  = savedContacts,
            isOnline  = isOnline,
            safePin   = safePin,
            onCancel  = { navigator?.pop() },
        )
    }
}
