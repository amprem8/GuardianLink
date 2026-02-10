package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ui.LoginScreenComposable

class LoginScreenActivity : Screen {
    @Composable
    override fun Content() {
        LoginScreenComposable()
    }
}