package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.delay
import screenmodel.TriggerConfigScreenModel
import storage.TriggerConfigStorage
import ui.TriggerConfigActions
import ui.TriggerConfigScreen
import ui.TriggerConfigUiState

class TriggerConfigActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { TriggerConfigScreenModel() }

        val voicePhrase by model.voicePhrase.collectAsState()
        val gestureType by model.gestureType.collectAsState()
        val useCustomPhrase by model.useCustomPhrase.collectAsState()
        val customPhrase by model.customPhrase.collectAsState()
        val isRecording by model.isRecording.collectAsState()
        val error by model.error.collectAsState()

        // Auto-stop recording after 2 seconds
        LaunchedEffect(isRecording) {
            if (isRecording) {
                delay(2000)
                model.stopRecording()
            }
        }

        val state = TriggerConfigUiState(
            voicePhrase = voicePhrase,
            gestureType = gestureType,
            useCustomPhrase = useCustomPhrase,
            customPhrase = customPhrase,
            isRecording = isRecording,
            error = error,
            isValid = model.isValid,
        )

        val actions = TriggerConfigActions(
            onSetVoicePhrase = model::setVoicePhrase,
            onSetGestureType = model::setGestureType,
            onSetUseCustomPhrase = model::setUseCustomPhrase,
            onSetCustomPhrase = model::setCustomPhrase,
            onRefreshPhrase = model::refreshPhrase,
            onTestVoice = model::testVoice,
            onDismissError = model::dismissError,
            onSave = {
                if (model.save()) {
                    navigator?.replaceAll(HomeScreenActivity())
                }
            },
            onBack = { navigator?.pop() },
        )

        TriggerConfigScreen(
            state = state,
            actions = actions,
        )
    }
}
