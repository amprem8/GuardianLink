package screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashScreenModel : ScreenModel {

    private val _finished = MutableStateFlow(false)
    val finished = _finished.asStateFlow()

    init {
        screenModelScope.launch {
            delay(5500)
            _finished.value = true
        }
    }
}
