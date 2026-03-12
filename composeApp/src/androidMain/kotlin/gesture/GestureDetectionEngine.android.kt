@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package gesture

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.SystemClock
import kotlin.math.abs
import kotlin.math.sqrt

actual object GestureDetectionEngine {

    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private var activeGesture: String = ""
    private var onDetected: ((String) -> Boolean)? = null

    private var isStarted = false
    private var latestGyroMagnitude = 0f

    private val gravity = FloatArray(3)
    private var noiseFloor = 0.8f

    private val tapTimestamps = ArrayDeque<Long>()
    private val shakeEvents = ArrayDeque<Pair<Long, Int>>()
    private val volumeDownTimestamps = ArrayDeque<Long>()

    private var cooldownUntil = 0L

    fun init(context: Context) {
        appContext = context.applicationContext
        val sm = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager = sm
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    actual fun start(gestureType: String, onDetected: (String) -> Boolean): Boolean {
        if (!::appContext.isInitialized) return false

        stop()
        resetState()

        activeGesture = gestureType
        this.onDetected = onDetected

        // Volume-triple-down is key-event based and does not require sensors.
        if (gestureType == "volume-triple-down") {
            isStarted = true
            return true
        }

        val sm = sensorManager ?: return false
        val accel = accelerometer ?: return false

        sm.registerListener(sensorListener, accel, SensorManager.SENSOR_DELAY_GAME)
        gyroscope?.let { sm.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME) }

        isStarted = true
        return true
    }

    actual fun stop() {
        if (!isStarted) return
        sensorManager?.unregisterListener(sensorListener)
        isStarted = false
        activeGesture = ""
        onDetected = null
        resetState()
    }

    actual fun notifyVolumeDownPress() {
        if (!isStarted || activeGesture != "volume-triple-down") return

        val now = SystemClock.elapsedRealtime()
        trimOld(volumeDownTimestamps, now - 1300L)
        volumeDownTimestamps.addLast(now)

        if (volumeDownTimestamps.size >= 3) {
            emitDetected()
            volumeDownTimestamps.clear()
        }
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!isStarted) return

            when (event.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    latestGyroMagnitude = sqrt(x * x + y * y + z * z)
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // High-pass filter: estimate gravity then subtract it for linear acceleration.
                    gravity[0] = 0.9f * gravity[0] + 0.1f * x
                    gravity[1] = 0.9f * gravity[1] + 0.1f * y
                    gravity[2] = 0.9f * gravity[2] + 0.1f * z

                    val lx = x - gravity[0]
                    val ly = y - gravity[1]
                    val lz = z - gravity[2]

                    val magnitude = sqrt(lx * lx + ly * ly + lz * lz)

                    // Adaptive floor for bulky case damping + per-device sensor noise.
                    noiseFloor = 0.96f * noiseFloor + 0.04f * magnitude

                    val now = SystemClock.elapsedRealtime()
                    val tapThreshold = maxOf(4.0f, noiseFloor * 3.8f)
                    val shakeThreshold = maxOf(8.0f, noiseFloor * 5.5f)

                    when (activeGesture) {
                        "double-tap" -> detectBackTap(now, magnitude, tapThreshold, tapsNeeded = 2)
                        "triple-tap" -> detectBackTap(now, magnitude, tapThreshold, tapsNeeded = 3)
                        "shake" -> detectShake(now, lx, ly, lz, magnitude, shakeThreshold)
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private fun detectBackTap(now: Long, magnitude: Float, threshold: Float, tapsNeeded: Int) {
        if (now < cooldownUntil) return

        // Back taps are short impulses. Reject heavy whole-phone motion via gyro.
        if (magnitude < threshold) return
        if (latestGyroMagnitude > 6.0f) return

        trimOld(tapTimestamps, now - 1100L)

        // Refractory period removes double-counting of a single physical tap pulse.
        if (tapTimestamps.isNotEmpty() && now - tapTimestamps.last() < 120L) return

        tapTimestamps.addLast(now)

        val window = if (tapsNeeded == 2) 650L else 950L
        val first = tapTimestamps.firstOrNull() ?: return

        if (tapTimestamps.size >= tapsNeeded && now - first <= window) {
            emitDetected()
            tapTimestamps.clear()
        }
    }

    private fun detectShake(now: Long, lx: Float, ly: Float, lz: Float, magnitude: Float, threshold: Float) {
        if (now < cooldownUntil) return
        if (magnitude < threshold) return

        val dominantAxis = dominantAxisSign(lx, ly, lz)
        trimOldShake(now - 1300L)
        shakeEvents.addLast(now to dominantAxis)

        // Count direction flips in recent high-energy peaks.
        var flips = 0
        var previous: Int? = null
        shakeEvents.forEach { (_, axisSign) ->
            if (previous != null && axisSign != previous) flips++
            previous = axisSign
        }

        if (flips >= 3 && shakeEvents.size >= 4) {
            emitDetected()
            shakeEvents.clear()
        }
    }

    private fun dominantAxisSign(x: Float, y: Float, z: Float): Int {
        val ax = abs(x)
        val ay = abs(y)
        val az = abs(z)
        return when {
            ax >= ay && ax >= az -> if (x >= 0) 1 else -1
            ay >= ax && ay >= az -> if (y >= 0) 2 else -2
            else -> if (z >= 0) 3 else -3
        }
    }

    private fun emitDetected() {
        val now = SystemClock.elapsedRealtime()
        if (now < cooldownUntil) return

        cooldownUntil = now + 1800L
        val shouldAcknowledge = onDetected?.invoke(activeGesture) ?: true
        if (shouldAcknowledge) {
            vibrateSuccess()
        }
    }

    private fun resetState() {
        tapTimestamps.clear()
        shakeEvents.clear()
        volumeDownTimestamps.clear()
        gravity.fill(0f)
        noiseFloor = 0.8f
        latestGyroMagnitude = 0f
        cooldownUntil = 0L
    }

    private fun trimOld(queue: ArrayDeque<Long>, minTimestamp: Long) {
        while (queue.isNotEmpty() && queue.first() < minTimestamp) queue.removeFirst()
    }

    private fun trimOldShake(minTimestamp: Long) {
        while (shakeEvents.isNotEmpty() && shakeEvents.first().first < minTimestamp) {
            shakeEvents.removeFirst()
        }
    }

    private fun vibrateSuccess() {
        if (!::appContext.isInitialized) return

        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(60L, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(60L)
        }
    }
}

