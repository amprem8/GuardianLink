package session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory holder for the latest inbound SOS alert payload. */
object SosAlertSession {

    data class Alert(
        val sosId: String,
        val victimName: String,
        val helpText: String,
        val lat: Double? = null,
        val lng: Double? = null,
    )

    private val _pendingAlert = MutableStateFlow<Alert?>(null)
    val pendingAlert: StateFlow<Alert?> = _pendingAlert.asStateFlow()

    fun set(alert: Alert) {
        _pendingAlert.value = alert
    }

    fun clear() {
        _pendingAlert.value = null
    }
}

