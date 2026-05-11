package screens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import location.CurrentLocation
import location.getCurrentLocationOrNull
import network.NetworkConnectivityObserver
import network.SosPushApi
import permissions.rememberLocationPermission
import push.refreshPushRegistrationBeforeSos
import storage.AppStorage
import storage.ContactStorage
import ui.ActiveSOSScreen
import ui.OFFLINE_MODE_NORMAL_SMS
import util.nowTimestampText
class ActiveSOSActivity(
    private val offlineFallbackMode: String = OFFLINE_MODE_NORMAL_SMS,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val isOnline by NetworkConnectivityObserver.isOnline.collectAsState()
        val savedContacts = ContactStorage.loadContacts()
        val safePin = AppStorage.getSafePin()
        val locationPermission = rememberLocationPermission()
        // Use remember (NOT rememberSaveable) — each new screen entry is a fresh SOS trigger
        var stopUiTimer by remember { mutableStateOf(false) }
        var pushResponse by remember { mutableStateOf<SosPushApi.SosPushResponse?>(null) }
        var pushError by remember { mutableStateOf<String?>(null) }
        // Unit key — runs exactly once per screen entry, not re-triggered by recompositions
        LaunchedEffect(Unit) {
            if (savedContacts.isEmpty()) {
                pushError = "No emergency contacts configured"
                return@LaunchedEffect
            }
            val victimName = AppStorage.getUserName().ifBlank { "User" }
            val victimUserId = AppStorage.getUserEmail()
                .ifBlank { AppStorage.getPhoneNumber().ifBlank { "user" } }
            // Await token refresh so SNS endpoint is up-to-date before we call /sos/push
            runCatching { refreshPushRegistrationBeforeSos() }
            // Fetch fresh location every single SOS trigger
            // Fall back to last persisted location when GPS is off or permission is denied
            val liveLocation: CurrentLocation? =
                if (locationPermission.isGranted) getCurrentLocationOrNull() else null
            val resolvedLat = liveLocation?.latitude ?: AppStorage.getLastKnownLat()
            val resolvedLng = liveLocation?.longitude ?: AppStorage.getLastKnownLng()
            val location = SosPushApi.SosLocationContext(
                permissionGranted = locationPermission.isGranted,
                gpsEnabled = liveLocation != null,
                lat = resolvedLat,
                lng = resolvedLng,
            )
            runCatching {
                SosPushApi.trigger(
                    SosPushApi.SosPushRequest(
                        victimUserId = victimUserId,
                        victimName = victimName,
                        location = location,
                        contacts = savedContacts.map { c ->
                            SosPushApi.SosContactTarget(
                                contactName = c.name,
                                phoneNumber = c.phone,
                                endpointArn = "",
                                includeGPS = c.includeGPS,
                            )
                        },
                    )
                )
            }.onSuccess { response ->
                pushResponse = response
                if (response.sentCount > 0) {
                    AppStorage.setLastSosSentText("Sent at ${nowTimestampText()}")
                }
                if (response.allPublished) {
                    stopUiTimer = true
                }
            }.onFailure { error ->
                stopUiTimer = true
                pushError = formatPushError(error)
            }
        }
        ActiveSOSScreen(
            contacts = savedContacts,
            isOnline = isOnline,
            offlineFallbackMode = offlineFallbackMode,
            safePin = safePin,
            stopTimer = stopUiTimer,
            pushResponse = pushResponse,
            pushError = pushError,
            onCancel = { navigator?.pop() },
        )
    }

    private fun formatPushError(error: Throwable): String {
        val message = error.message?.trim().orEmpty()
        return when {
            message.isBlank() -> error::class.simpleName ?: "Push failed"
            message.contains("input length", ignoreCase = true) ->
                "Unable to read the SOS response. Please retry after refreshing push registration."
            else -> message
        }
    }
}
