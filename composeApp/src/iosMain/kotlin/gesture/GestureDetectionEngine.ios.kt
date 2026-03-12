@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package gesture

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMAccelerometerData
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.posix.time
import kotlin.math.abs
import kotlin.math.sqrt

@OptIn(ExperimentalForeignApi::class)
actual object GestureDetectionEngine {

    private val motionManager = CMMotionManager()
    private val queue: NSOperationQueue = NSOperationQueue.mainQueue()

    private var activeGesture: String = ""
    private var onDetected: ((String) -> Unit)? = null

    private val tapTimestamps = ArrayDeque<Long>()
    private val shakeEvents = ArrayDeque<Pair<Long, Int>>()

    private val gravity = FloatArray(3)
    private var noiseFloor = 0.6f
    private var cooldownUntil = 0L

    actual fun start(gestureType: String, onDetected: (String) -> Unit): Boolean {
        stop()

        activeGesture = gestureType
        this.onDetected = onDetected

        // iOS has no public API for raw hardware volume-button events in KMP.
        if (gestureType == "volume-triple-down") return false

        if (!motionManager.accelerometerAvailable) return false
        motionManager.accelerometerUpdateInterval = 0.02 // 50 Hz

        motionManager.startAccelerometerUpdatesToQueue(queue) { data: CMAccelerometerData?, _ ->
            val sample = data ?: return@startAccelerometerUpdatesToQueue
            handleAcceleration(sample)
        }

        return true
    }

    actual fun stop() {
        if (motionManager.accelerometerActive) {
            motionManager.stopAccelerometerUpdates()
        }
        activeGesture = ""
        onDetected = null
        tapTimestamps.clear()
        shakeEvents.clear()
        gravity.fill(0f)
        noiseFloor = 0.6f
        cooldownUntil = 0L
    }

    actual fun notifyVolumeDownPress() {
        // Not supported on iOS in this KMP layer.
    }

    private fun handleAcceleration(data: CMAccelerometerData) {
        val now = nowMs()
        if (now < cooldownUntil) return

        val accel = data.acceleration.useContents { Triple(x.toFloat(), y.toFloat(), z.toFloat()) }
        val x = accel.first * 9.81f
        val y = accel.second * 9.81f
        val z = accel.third * 9.81f

        gravity[0] = 0.9f * gravity[0] + 0.1f * x
        gravity[1] = 0.9f * gravity[1] + 0.1f * y
        gravity[2] = 0.9f * gravity[2] + 0.1f * z

        val lx = x - gravity[0]
        val ly = y - gravity[1]
        val lz = z - gravity[2]

        val magnitude = sqrt(lx * lx + ly * ly + lz * lz)
        noiseFloor = 0.96f * noiseFloor + 0.04f * magnitude

        val tapThreshold = maxOf(3.2f, noiseFloor * 3.4f)
        val shakeThreshold = maxOf(7.5f, noiseFloor * 5.0f)

        when (activeGesture) {
            "double-tap" -> detectBackTap(now, magnitude, tapThreshold, tapsNeeded = 2)
            "triple-tap" -> detectBackTap(now, magnitude, tapThreshold, tapsNeeded = 3)
            "shake" -> detectShake(now, lx, ly, lz, magnitude, shakeThreshold)
        }
    }

    private fun detectBackTap(now: Long, magnitude: Float, threshold: Float, tapsNeeded: Int) {
        if (magnitude < threshold) return

        trimOld(tapTimestamps, now - 1100L)
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
        if (magnitude < threshold) return

        val dominantAxis = dominantAxisSign(lx, ly, lz)
        while (shakeEvents.isNotEmpty() && shakeEvents.first().first < now - 1300L) {
            shakeEvents.removeFirst()
        }
        shakeEvents.addLast(now to dominantAxis)

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
        cooldownUntil = nowMs() + 1800L

        val generator = UINotificationFeedbackGenerator()
        generator.prepare()
        generator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)

        onDetected?.invoke(activeGesture)
    }

    private fun trimOld(queue: ArrayDeque<Long>, minTimestamp: Long) {
        while (queue.isNotEmpty() && queue.first() < minTimestamp) queue.removeFirst()
    }

    private fun nowMs(): Long = time(null) * 1000L
}






