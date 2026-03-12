package audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sqrt

actual class VoicePhraseRecorder actual constructor() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var appContext: Context

        /** Call once from MainActivity.onCreate() */
        fun init(context: Context) {
            appContext = context.applicationContext
        }
    }

    // ── MediaRecorder for high-quality M4A output ────────────────────
    private var mediaRecorder: MediaRecorder? = null

    // ── AudioRecord for real-time analysis ──────────────────────────
    private var audioRecord: AudioRecord? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var analysisJob: Job? = null

    private val SAMPLE_RATE    = 44100
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT   = AudioFormat.ENCODING_PCM_16BIT
    private val FRAME_SIZE     = 2048        // samples per analysis frame
    private val ANALYSIS_STRIDE = SAMPLE_RATE / 10  // ~100 ms stride → 10 fps

    private var outputPath = ""

    @SuppressLint("MissingPermission")
    actual fun start(
        outputPath: String,
        onPitch: (Float) -> Unit,
        onBass:  (Float) -> Unit,
    ) {
        this.outputPath = outputPath

        // ── 1. MediaRecorder: AAC-LC into M4A ────────────────────────
        // On API 31+ the constructor requires a real Context — use the stored appContext.
        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(appContext)          // ← real context, NOT Application()
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        mr.apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44100)
            setAudioChannels(1)
            setOutputFile(outputPath)
            prepare()
            start()
        }
        mediaRecorder = mr

        // ── 2. AudioRecord: parallel PCM for analysis ───────────────
        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufSize = maxOf(minBuf, FRAME_SIZE * 2 * 4)
        val ar = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufSize,
        )
        audioRecord = ar

        // Attach hardware noise suppressor if available
        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(ar.audioSessionId)
            noiseSuppressor?.enabled = true
        }

        ar.startRecording()

        // ── 3. Coroutine: read PCM → pitch + bass ───────────────────
        analysisJob = CoroutineScope(Dispatchers.IO).launch {
            val buffer = ShortArray(FRAME_SIZE)
            var accumulated = 0

            while (isActive) {
                val read = ar.read(buffer, 0, FRAME_SIZE)
                if (read <= 0) continue

                accumulated += read
                if (accumulated < ANALYSIS_STRIDE) continue
                accumulated = 0

                val floats = FloatArray(read) { buffer[it] / 32768f }

                // Apply Hann window to reduce spectral leakage
                val windowed = applyHannWindow(floats)

                val pitch = estimatePitchYin(windowed, SAMPLE_RATE)
                val bass  = computeBassRms(windowed, SAMPLE_RATE)

                onPitch(pitch)
                onBass(bass)
            }
        }
    }

    actual fun stop(): String {
        analysisJob?.cancel()
        analysisJob = null

        noiseSuppressor?.release()
        noiseSuppressor = null

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null

        return outputPath
    }

    actual fun release() {
        stop()
    }

    // ────────────────────────────────────────────────────────────────
    // Signal processing helpers
    // ────────────────────────────────────────────────────────────────

    /** Hann (Hanning) window to reduce spectral leakage. */
    private fun applyHannWindow(samples: FloatArray): FloatArray {
        val n = samples.size
        return FloatArray(n) { i ->
            samples[i] * (0.5f - 0.5f * cos(2.0 * PI * i / (n - 1)).toFloat())
        }
    }

    /**
     * YIN pitch estimation algorithm.
     * Works well at all volumes — it does NOT rely on amplitude threshold,
     * it uses the difference function which is robust to silence and noise.
     *
     * Returns estimated pitch in Hz, or 0f if no clear pitch found.
     */
    private fun estimatePitchYin(samples: FloatArray, sampleRate: Int): Float {
        val n = samples.size
        val halfN = n / 2
        val threshold = 0.10f   // lower = more sensitive (0.10 works in noisy environments)

        // Step 1: difference function d(τ)
        val diff = FloatArray(halfN)
        for (tau in 1 until halfN) {
            var sum = 0f
            for (j in 0 until halfN) {
                val delta = samples[j] - samples[j + tau]
                sum += delta * delta
            }
            diff[tau] = sum
        }

        // Step 2: cumulative mean normalised difference
        val cmndf = FloatArray(halfN)
        cmndf[0] = 1f
        var runningSum = 0f
        for (tau in 1 until halfN) {
            runningSum += diff[tau]
            cmndf[tau] = if (runningSum == 0f) 1f else diff[tau] * tau / runningSum
        }

        // Step 3: find first dip below threshold
        var tau = 2
        while (tau < halfN - 1) {
            if (cmndf[tau] < threshold) {
                // Parabolic interpolation for sub-sample accuracy
                val better = parabolicInterp(cmndf, tau)
                val freq = sampleRate / better
                return if (freq in 60f..1000f) freq else 0f  // voice range 60–1000 Hz
            }
            tau++
        }

        // Step 4: if no dip found, pick the global minimum
        var minVal = Float.MAX_VALUE
        var minTau = 2
        for (t in 2 until halfN) {
            if (cmndf[t] < minVal) { minVal = cmndf[t]; minTau = t }
        }
        return if (minVal < 0.3f) {
            val freq = sampleRate / parabolicInterp(cmndf, minTau)
            if (freq in 60f..1000f) freq else 0f
        } else 0f
    }

    private fun parabolicInterp(arr: FloatArray, pos: Int): Float {
        if (pos <= 0 || pos >= arr.size - 1) return pos.toFloat()
        val s0 = arr[pos - 1]; val s1 = arr[pos]; val s2 = arr[pos + 1]
        val denom = 2f * (s0 - 2f * s1 + s2)
        return if (denom == 0f) pos.toFloat()
        else pos - (s2 - s0) / denom
    }

    /**
     * Compute normalised RMS energy in the bass band (20–300 Hz).
     *
     * Uses a simple recursive IIR low-pass filter (Butterworth approximation)
     * to extract the bass band, then computes RMS.
     * This is much cheaper than a full FFT but equally robust.
     *
     * Returns a value 0..1.
     */
    private fun computeBassRms(samples: FloatArray, sampleRate: Int): Float {
        // IIR low-pass at 300 Hz  (fc = 300, fs = sampleRate)
        val fc = 300.0
        val omega = 2.0 * PI * fc / sampleRate
        val alpha = (1.0 - cos(omega)) / (1.0 + cos(omega))  // 1st-order IIR coeff

        val lowpassed = FloatArray(samples.size)
        var prev = 0f
        for (i in samples.indices) {
            val out = (alpha * samples[i] + (1.0 - alpha) * prev).toFloat()
            lowpassed[i] = out
            prev = out
        }

        // RMS
        var sumSq = 0.0
        for (v in lowpassed) sumSq += v.toDouble() * v
        val rms = sqrt(sumSq / lowpassed.size).toFloat()

        // Normalise: typical speech bass RMS ~0.05–0.2 → map to 0..1
        return (rms / 0.2f).coerceIn(0f, 1f)
    }
}


