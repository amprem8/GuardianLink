@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package gesture

/**
 * Cross-platform real-time gesture detector used by:
 * 1) Trigger Configuration live testing
 * 2) Android continuous monitoring service
 */
expect object GestureDetectionEngine {

    /**
     * Start listening for one selected gesture.
     *
     * @return true when listener starts successfully, false when unavailable.
     */
    fun start(gestureType: String, onDetected: (String) -> Unit): Boolean

    /** Stop listening and release resources. */
    fun stop()

    /**
     * Platform hook for hardware volume-down presses.
     * Android forwards events from Activity.dispatchKeyEvent.
     */
    fun notifyVolumeDownPress()
}

