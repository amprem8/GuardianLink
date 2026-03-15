package audio

/**
 * Cross-platform voice phrase recorder.
 *
 * Records audio to a temp .m4a file and streams real-time
 * pitch (Hz) and bass (0–1 normalised RMS of low-frequency band)
 * via callbacks while recording.
 *
 * Usage:
 *   recorder.start(outputPath, onPitch, onBass)
 *   // ... user speaks ...
 *   val file = recorder.stop()   // returns absolute path to .m4a
 */
expect class VoicePhraseRecorder() {

    /**
     * Start recording.
     * @param outputPath   Absolute path to write the .m4a file
     * @param onPitch      Called ~10× per second with estimated pitch in Hz (0 = unvoiced)
     * @param onBass       Called ~10× per second with normalised bass energy 0..1
     */
    fun start(
        outputPath: String,
        onPitch: (Float) -> Unit,
        onBass: (Float) -> Unit,
    )

    /** Stop recording and return the path of the finished .m4a file. */
    fun stop(): String

    /** Release any held resources. */
    fun release()
}

