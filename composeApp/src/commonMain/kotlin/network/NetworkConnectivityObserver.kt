@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package network

import kotlinx.coroutines.flow.StateFlow

/**
 * Platform-specific real-time network connectivity observer.
 *
 * - Android: ConnectivityManager + NetworkCallback (ACCESS_NETWORK_STATE)
 * - iOS: NWPathMonitor
 *
 * Provides a [StateFlow] that emits `true` when the device has internet
 * connectivity and `false` when offline. Updates are pushed in real-time
 * with minimal latency for fast icon changes.
 */
expect object NetworkConnectivityObserver {

    /**
     * Real-time connectivity state.
     * `true` = online, `false` = offline.
     * Emits immediately upon collection and on every connectivity change.
     */
    val isOnline: StateFlow<Boolean>

    /**
     * Start observing network changes. Must be called once from
     * [MainActivity.onCreate] on Android. No-op on iOS (auto-starts).
     */
    fun start()

    /**
     * Stop observing and release resources.
     */
    fun stop()
}
