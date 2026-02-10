package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import ui.AuthScreenComposable

class AuthScreenActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        AuthScreenComposable(
            onSignup = {
               /*later*/
            },
            onLogin = {
                navigator?.push(LoginScreenActivity())
            }
        )
    }
}