package screens

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ui.SignupScreen

class SignupScreenActivity : Screen {
    @Composable
    override fun Content() {
        SignupScreen()
    }
}