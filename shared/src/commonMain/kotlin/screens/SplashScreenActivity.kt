package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screenmodel.SplashScreenModel
import ui.SplashScreenContent

class SplashScreenActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { SplashScreenModel() }
        val finished by model.finished.collectAsState()

        LaunchedEffect(finished) {
            if (finished) {
                navigator?.replace(AuthScreenActivity())
            }
        }

        SplashScreenContent()
    }
}