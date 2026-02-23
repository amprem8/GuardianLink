package screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import storage.AppStorage

sealed class SplashDestination {
    object None : SplashDestination()
    object Auth : SplashDestination()       // never registered → signup
    object Login : SplashDestination()      // registered but logged out → login
    object Contacts : SplashDestination()   // logged in but contacts not set up
    object Home : SplashDestination()       // fully set up → home
}

class SplashScreenModel : ScreenModel {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination = _destination.asStateFlow()

    init {
        screenModelScope.launch {
            delay(5500)
            _destination.value = when {
                AppStorage.isLoggedIn() && AppStorage.isContactsConfigured() -> SplashDestination.Home
                AppStorage.isLoggedIn()  -> SplashDestination.Contacts
                AppStorage.isRegistered() -> SplashDestination.Login
                else                      -> SplashDestination.Auth
            }
        }
    }
}
