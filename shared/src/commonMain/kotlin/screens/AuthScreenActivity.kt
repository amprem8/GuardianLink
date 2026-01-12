package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import ui.AuthScreenContent
import ui.LoginScreen

class AuthScreenActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        AuthScreenContent(
            onSignup = {
               /*later*/
            },
            onLogin = {
                navigator?.push(LoginScreen())
            }
        )
    }
}
