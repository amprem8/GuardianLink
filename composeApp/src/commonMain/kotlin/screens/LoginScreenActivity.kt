package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screenmodel.LoginScreenModel
import screenmodel.LoginUiState
import session.UserSession
import storage.AppStorage
import ui.LoginScreen

class LoginScreenActivity : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { LoginScreenModel() }
        val uiState by model.uiState.collectAsState()

        // On login success → mark logged in + go to Home
        LaunchedEffect(uiState) {
            if (uiState is LoginUiState.Success) {
                AppStorage.setLoggedIn(true)
                navigator?.replaceAll(HomeScreenActivity())
            }
        }

        LoginScreen(
            isLoading = uiState is LoginUiState.Loading,
            error = (uiState as? LoginUiState.Error)?.message.orEmpty(),
            onLogin = { email, password -> model.login(email, password) },
            onDismissError = model::dismissError,
            onBack = { navigator?.pop() },
            onSignup = { navigator?.push(SignupScreenActivity()) },
            onForgotPassword = { /* TODO: Forgot password flow */ }
        )
    }
}
