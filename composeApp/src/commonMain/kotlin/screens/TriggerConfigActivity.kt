package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import screenmodel.TriggerConfigScreenModel
import ui.TriggerConfigActions
import ui.TriggerConfigScreen
import ui.TriggerConfigUiState

expect fun tempAudioFilePath(): String

class TriggerConfigActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { TriggerConfigScreenModel() }

        val voicePhrase     by model.voicePhrase.collectAsState()
        val gestureType     by model.gestureType.collectAsState()
        val useCustomPhrase by model.useCustomPhrase.collectAsState()
        val customPhrase    by model.customPhrase.collectAsState()
        val isRecording     by model.isRecording.collectAsState()
        val error           by model.error.collectAsState()
        val uploadState     by model.uploadState.collectAsState()

        val state = TriggerConfigUiState(
            voicePhrase      = voicePhrase,
            gestureType      = gestureType,
            useCustomPhrase  = useCustomPhrase,
            customPhrase     = customPhrase,
            isRecording      = isRecording,
            error            = error,
            isValid          = model.isValid,
            uploadState      = uploadState,
        )

        val actions = TriggerConfigActions(
            onSetVoicePhrase     = model::setVoicePhrase,
            onSetGestureType     = model::setGestureType,
            onSetUseCustomPhrase = model::setUseCustomPhrase,
            onSetCustomPhrase    = model::setCustomPhrase,
            onRefreshPhrase      = model::refreshPhrase,
            onTestVoice          = { model.testVoice(tempAudioFilePath()) },
            onStopRecording      = model::stopRecording,
            onDismissError       = model::dismissError,
            onDismissUploadError = model::dismissUploadState,
            onSave = {
                if (model.save()) navigator?.replaceAll(HomeScreenActivity())
            },
            onBack = { navigator?.pop() },
        )

        TriggerConfigScreen(state = state, actions = actions)
    }
}
