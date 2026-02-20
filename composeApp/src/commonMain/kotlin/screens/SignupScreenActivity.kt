package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screenmodel.SignupScreenModel
import screenmodel.SignupUiState
import ui.SignupScreen

class SignupScreenActivity : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { SignupScreenModel() }
        val uiState by model.uiState.collectAsState()

        // Navigate to OTP screen only when API returns success (201)
        LaunchedEffect(uiState) {
            if (uiState is SignupUiState.Success) {
                navigator?.push(OtpScreenActivity())
            }
        }

        SignupScreen(
            isLoading = uiState is SignupUiState.Loading,
            error = (uiState as? SignupUiState.Error)?.message.orEmpty(),
            onSignup = { name, email, password, confirmPassword, agreeToTerms ->
                model.signup(name, email, password, confirmPassword, agreeToTerms)
            },
            onDismissError = model::dismissError
        )
    }
}
