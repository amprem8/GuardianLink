package screenmodel

import audio.VoicePhraseRecorder
import audio.VoicePhraseUploader
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.TriggerConfig
import storage.AppStorage
import storage.TriggerConfigStorage

val PRESET_PHRASES = listOf(
    "Help Me", "Emergency", "SOS", "I Need Help", "Danger",
    "Bachaao", "Help Please", "Alert", "Save Me",
)

// ── Upload state ─────────────────────────────────────────────

sealed class UploadState {
    object Idle      : UploadState()
    object Uploading : UploadState()
    data class Success(val s3Key: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

class TriggerConfigScreenModel : ScreenModel {

    private val initial = TriggerConfigStorage.loadConfig()

    private val _voicePhrase     = MutableStateFlow(initial.voicePhrase)
    val voicePhrase = _voicePhrase.asStateFlow()

    private val _gestureType     = MutableStateFlow(initial.gestureType)
    val gestureType = _gestureType.asStateFlow()

    private val _useCustomPhrase = MutableStateFlow(initial.useCustomPhrase)
    val useCustomPhrase = _useCustomPhrase.asStateFlow()

    private val _customPhrase    = MutableStateFlow(if (initial.useCustomPhrase) initial.voicePhrase else "")
    val customPhrase = _customPhrase.asStateFlow()

    private val _isRecording     = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _error           = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _uploadState     = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState = _uploadState.asStateFlow()

    // ── Recording constants ──────────────────────────────────
    // Hard cap: 3 s (a keyword like "Alert" takes < 1 s)
    private val MAX_RECORDING_MS   = 3_000L
    // After voice is detected, wait 600 ms of tail then stop
    private val TAIL_AFTER_VOICE_MS = 600L
    // RMS level above this = voice detected (0..1 scale from recorder)
    private val VOICE_THRESHOLD    = 0.15f

    private var recorder: VoicePhraseRecorder? = null
    private var recordingTimerJob: Job? = null
    private var voiceDetectedJob: Job? = null
    private var voiceDetected = false

    // ── Derived helpers ──────────────────────────────────────

    val isValid: Boolean
        get() = if (_useCustomPhrase.value) _customPhrase.value.trim().isNotEmpty()
                else _voicePhrase.value.trim().isNotEmpty()

    private val activePhrase: String
        get() = if (_useCustomPhrase.value) _customPhrase.value.trim()
                else _voicePhrase.value.trim()

    // ── Setters ──────────────────────────────────────────────

    fun setVoicePhrase(phrase: String)       { _voicePhrase.value = phrase }
    fun setGestureType(type: String)         { _gestureType.value = type }
    fun setUseCustomPhrase(custom: Boolean)  { _useCustomPhrase.value = custom }
    fun setCustomPhrase(phrase: String)      { _customPhrase.value = phrase }

    fun refreshPhrase() {
        val available = PRESET_PHRASES.filter { it != _voicePhrase.value }
        if (available.isNotEmpty()) _voicePhrase.value = available.random()
    }

    // ── Recording ────────────────────────────────────────────

    /**
     * Start recording into [outputPath] (.m4a).
     * Recording auto-stops after MAX_RECORDING_SECONDS and upload begins.
     */
    fun testVoice(outputPath: String) {
        if (_isRecording.value) return

        _uploadState.value = UploadState.Idle
        voiceDetected      = false

        recorder = VoicePhraseRecorder()
        recorder!!.start(
            outputPath = outputPath,
            // onPitch carries the audio level (0..1) from the recorder
            onPitch = { level ->
                if (!voiceDetected && level >= VOICE_THRESHOLD) {
                    voiceDetected = true
                    // Voice detected → wait tail then stop
                    voiceDetectedJob = screenModelScope.launch {
                        delay(TAIL_AFTER_VOICE_MS)
                        stopAndUpload()
                    }
                }
            },
            onBass = { /* unused */ },
        )
        _isRecording.value = true

        // Hard-cap timer: stop after 3 s no matter what
        recordingTimerJob = screenModelScope.launch {
            delay(MAX_RECORDING_MS)
            stopAndUpload()
        }
    }

    /** Called when user taps Stop manually. */
    fun stopRecording() {
        recordingTimerJob?.cancel()
        voiceDetectedJob?.cancel()
        recordingTimerJob = null
        voiceDetectedJob  = null
        stopAndUpload()
    }

    private fun stopAndUpload() {
        // Guard: only run once even if both timers fire
        if (!_isRecording.value) return

        recordingTimerJob?.cancel()
        voiceDetectedJob?.cancel()
        recordingTimerJob = null
        voiceDetectedJob  = null

        val path = recorder?.stop() ?: run { _isRecording.value = false; return }
        recorder?.release()
        recorder = null
        _isRecording.value = false

        if (path.isBlank()) return

        // Upload regardless — the file always contains whatever was recorded.
        // The server stores the audio as-is; silence is fine (user can re-test).
        uploadToS3(path)
    }

    private fun uploadToS3(audioPath: String) {
        val phrase     = activePhrase
        val username   = AppStorage.getUserName().ifBlank { AppStorage.getUserEmail().ifBlank { "user" } }
        val backendUrl = AppStorage.getBackendBaseUrl()

        _uploadState.value = UploadState.Uploading

        screenModelScope.launch {
            try {
                val s3Key = VoicePhraseUploader().upload(
                    backendBaseUrl = backendUrl,
                    username       = username,
                    phrase         = phrase,
                    audioFilePath  = audioPath,
                )
                _uploadState.value = UploadState.Success(s3Key)
                // Auto-dismiss toast after 4 s
                delay(3_500)
                if (_uploadState.value is UploadState.Success) _uploadState.value = UploadState.Idle
            } catch (e: Exception) {
                // Never expose raw technical strings (hostnames, stack traces) to the user
                val friendlyMessage = when {
                    e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("No address associated",  ignoreCase = true) == true ||
                    e.message?.contains("UnknownHostException",   ignoreCase = true) == true ->
                        "No internet connection. Check your network and try again."

                    e.message?.contains("timeout", ignoreCase = true) == true ||
                    e.message?.contains("timed out", ignoreCase = true) == true ->
                        "Upload timed out. Check your connection and try again."

                    e.message?.contains("403", ignoreCase = true) == true ||
                    e.message?.contains("Forbidden", ignoreCase = true) == true ->
                        "Upload permission denied. Please try again later."

                    e.message?.contains("404", ignoreCase = true) == true ->
                        "Upload destination not found. Please try again later."

                    e.message?.contains("HTTP 5", ignoreCase = true) == true ||
                    e.message?.contains("500", ignoreCase = true) == true ->
                        "Server error. Please try again in a moment."

                    else ->
                        "Upload failed. Please check your connection and try again."
                }
                _uploadState.value = UploadState.Error(friendlyMessage)
            }
        }
    }

    fun dismissUploadState() { _uploadState.value = UploadState.Idle }
    fun dismissError()       { _error.value = "" }

    // ── Save ─────────────────────────────────────────────────

    fun save(): Boolean {
        val finalPhrase = activePhrase
        if (finalPhrase.isEmpty()) { _error.value = "Please select or enter a voice phrase"; return false }
        TriggerConfigStorage.saveConfig(
            TriggerConfig(voicePhrase = finalPhrase, gestureType = _gestureType.value, useCustomPhrase = _useCustomPhrase.value)
        )
        return true
    }

    override fun onDispose() {
        recordingTimerJob?.cancel()
        voiceDetectedJob?.cancel()
        recorder?.release()
        recorder = null
        super.onDispose()
    }
}
