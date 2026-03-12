package audio

actual class VoicePhraseRecorder actual constructor() {

    actual fun start(
        outputPath: String,
        onPitch: (Float) -> Unit,
        onBass: (Float) -> Unit,
    ) {
        // iOS implementation: AVAudioRecorder + AVAudioEngine tap
        // Stubbed — will be implemented in a native Swift interop layer
    }

    actual fun stop(): String = ""

    actual fun release() {}
}

