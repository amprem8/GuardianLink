package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screenmodel.SplashDestination
import screenmodel.SplashScreenModel
import ui.SplashScreenContent

class SplashScreenActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { SplashScreenModel() }
        val destination by model.destination.collectAsState()

        LaunchedEffect(destination) {
            when (destination) {
                SplashDestination.Auth          -> navigator?.replace(AuthScreenActivity())
                SplashDestination.Login         -> navigator?.replace(LoginScreenActivity())
                SplashDestination.Contacts      -> navigator?.replace(EmergencyContactsActivity())
                SplashDestination.TriggerConfig -> navigator?.replace(TriggerConfigActivity())
                SplashDestination.Home          -> navigator?.replace(HomeScreenActivity())
                SplashDestination.None          -> { /* still showing splash */ }
            }
        }

        SplashScreenContent()
    }
}
