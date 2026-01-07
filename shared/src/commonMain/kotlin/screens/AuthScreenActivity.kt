package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screenmodel.AuthScreenModel
import ui.AuthScreenContent
import ui.LoginScreen

class AuthScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { AuthScreenModel() }

        AuthScreenContent(
            onSignup = {
                // later API / signup flow
            },
            onLogin = {
                navigator?.push(LoginScreen())
            }
        )
    }
}
