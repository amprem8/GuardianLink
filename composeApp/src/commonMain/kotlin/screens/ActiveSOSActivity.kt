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
import storage.AppStorage
import storage.ContactStorage
import ui.ActiveSOSScreen

class ActiveSOSActivity : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        val isOnline by NetworkConnectivityObserver.isOnline.collectAsState()
        val savedContacts = ContactStorage.loadContacts()
        val safePin = AppStorage.getSafePin()
        val locationPermission = rememberLocationPermission()

        var sosTriggered by remember { mutableStateOf(false) }
        var stopUiTimer by remember { mutableStateOf(false) }

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
                if (response.allPublished) {
                    stopUiTimer = true
                }
            }

            sosTriggered = true
        }

        ActiveSOSScreen(
            contacts  = savedContacts,
            isOnline  = isOnline,
            safePin   = safePin,
            stopTimer = stopUiTimer,
            onCancel  = { navigator?.pop() },
        )
    }
}
