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
import network.NetworkConnectivityObserver
import network.SosPushApi
import permissions.rememberLocationPermission
import push.refreshPushRegistrationBeforeSos
import storage.AppStorage
import storage.ContactStorage
import ui.ActiveSOSScreen
import ui.OFFLINE_MODE_NORMAL_SMS

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

        var sosTriggered by remember { mutableStateOf(false) }
        var stopUiTimer by remember { mutableStateOf(false) }
        var pushResponse by remember { mutableStateOf<SosPushApi.SosPushResponse?>(null) }
        var pushError by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(isOnline, locationPermission.isGranted, savedContacts) {
            if (sosTriggered || !isOnline || savedContacts.isEmpty()) return@LaunchedEffect

            val victimName = AppStorage.getUserName().ifBlank { "User" }
            val victimUserId = AppStorage.getUserEmail()
                .ifBlank { AppStorage.getPhoneNumber().ifBlank { "user" } }

            val location = if (locationPermission.isGranted) {
                // Location provider integration can replace this static fallback later.
                SosPushApi.SosLocationContext(
                    permissionGranted = true,
                    gpsEnabled = true,
                    lat = 12.9716,
                    lng = 77.5946,
                )
            } else {
                SosPushApi.SosLocationContext(
                    permissionGranted = false,
                    gpsEnabled = false,
                )
            }

            runCatching {
                refreshPushRegistrationBeforeSos()
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
                if (response.allPublished) {
                    stopUiTimer = true
                }
            }.onFailure { error ->
                pushError = error.message ?: error::class.simpleName ?: "Push failed"
            }

            sosTriggered = true
        }

        ActiveSOSScreen(
            contacts  = savedContacts,
            isOnline  = isOnline,
            offlineFallbackMode = offlineFallbackMode,
            safePin   = safePin,
            stopTimer = stopUiTimer,
            pushResponse = pushResponse,
            pushError = pushError,
            onCancel  = { navigator?.pop() },
        )
    }
}
