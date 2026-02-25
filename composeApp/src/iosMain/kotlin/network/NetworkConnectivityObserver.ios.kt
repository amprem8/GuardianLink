@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

actual object NetworkConnectivityObserver {

    private val _isOnline = MutableStateFlow(true)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var monitor: nw_path_monitor_t? = null
    private var isStarted = false

    actual fun start() {
        if (isStarted) return
        isStarted = true

        val pathMonitor = nw_path_monitor_create()
        monitor = pathMonitor

        nw_path_monitor_set_queue(pathMonitor, dispatch_get_main_queue())

        nw_path_monitor_set_update_handler(pathMonitor) { path ->
            val status = nw_path_get_status(path)
            _isOnline.value = (status == nw_path_status_satisfied)
        }

        nw_path_monitor_start(pathMonitor)
    }

    actual fun stop() {
        if (!isStarted) return
        isStarted = false
        monitor?.let { nw_path_monitor_cancel(it) }
        monitor = null
    }
}
