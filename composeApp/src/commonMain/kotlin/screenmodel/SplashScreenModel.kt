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
    object Auth : SplashDestination()
    object Home : SplashDestination()
}

class SplashScreenModel : ScreenModel {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination = _destination.asStateFlow()

    init {
        screenModelScope.launch {
            delay(5500)
            _destination.value = if (AppStorage.isRegistered()) {
                SplashDestination.Home
            } else {
                SplashDestination.Auth
            }
        }
    }
}
