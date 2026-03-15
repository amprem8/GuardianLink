package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import session.SosAlertSession
import ui.SosAlertDetailsScreen

class SosAlertDetailsActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val alert by SosAlertSession.pendingAlert.collectAsState()

        SosAlertDetailsScreen(
            alert = alert,
            onClose = {
                SosAlertSession.clear()
                navigator?.replace(HomeScreenActivity())
            },
        )
    }
}

