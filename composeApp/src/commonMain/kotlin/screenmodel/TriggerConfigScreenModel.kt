package screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.TriggerConfig
import storage.TriggerConfigStorage

val PRESET_PHRASES = listOf(
    "Help Me",
    "Emergency",
    "SOS",
    "I Need Help",
    "Danger",
    "Bachaao",
    "Help Please",
    "Alert",
    "Save Me",
)

class TriggerConfigScreenModel : ScreenModel {

    private val initial = TriggerConfigStorage.loadConfig()

    private val _voicePhrase = MutableStateFlow(initial.voicePhrase)
    val voicePhrase = _voicePhrase.asStateFlow()

    private val _gestureType = MutableStateFlow(initial.gestureType)
    val gestureType = _gestureType.asStateFlow()

    private val _useCustomPhrase = MutableStateFlow(initial.useCustomPhrase)
    val useCustomPhrase = _useCustomPhrase.asStateFlow()

    private val _customPhrase = MutableStateFlow(if (initial.useCustomPhrase) initial.voicePhrase else "")
    val customPhrase = _customPhrase.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    val isValid: Boolean
        get() = if (_useCustomPhrase.value) _customPhrase.value.trim().isNotEmpty()
        else _voicePhrase.value.trim().isNotEmpty()

    // ── Actions ─────────────────────────────────────────────

    fun setVoicePhrase(phrase: String) {
        _voicePhrase.value = phrase
    }

    fun setGestureType(type: String) {
        _gestureType.value = type
    }

    fun setUseCustomPhrase(custom: Boolean) {
        _useCustomPhrase.value = custom
    }

    fun setCustomPhrase(phrase: String) {
        _customPhrase.value = phrase
    }

    fun refreshPhrase() {
        val available = PRESET_PHRASES.filter { it != _voicePhrase.value }
        if (available.isNotEmpty()) {
            _voicePhrase.value = available.random()
        }
    }

    fun testVoice() {
        _isRecording.value = true
        // In a real app this would start voice recognition
        // For now just simulate a brief recording period
    }

    fun stopRecording() {
        _isRecording.value = false
    }

    fun dismissError() {
        _error.value = ""
    }

    fun save(): Boolean {
        val finalPhrase = if (_useCustomPhrase.value) _customPhrase.value.trim()
        else _voicePhrase.value.trim()

        if (finalPhrase.isEmpty()) {
            _error.value = "Please select or enter a voice phrase"
            return false
        }

        TriggerConfigStorage.saveConfig(
            TriggerConfig(
                voicePhrase = finalPhrase,
                gestureType = _gestureType.value,
                useCustomPhrase = _useCustomPhrase.value,
            )
        )
        return true
    }
}
