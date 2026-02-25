package permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter

// ── Location ────────────────────────────────────────────────

@Composable
actual fun rememberLocationPermission(): PermissionState {
    var isGranted by remember {
        val status = CLLocationManager.authorizationStatus()
        mutableStateOf(
            status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    status == kCLAuthorizationStatusAuthorizedAlways
        )
    }
    val manager = remember { CLLocationManager() }

    return PermissionState(
        isGranted = isGranted,
        launchRequest = {
            manager.requestWhenInUseAuthorization()
            // Re-check after a brief delay (the delegate callback updates asynchronously)
            val status = CLLocationManager.authorizationStatus()
            isGranted = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    status == kCLAuthorizationStatusAuthorizedAlways
        }
    )
}

// ── Microphone ──────────────────────────────────────────────

@Composable
actual fun rememberMicrophonePermission(): PermissionState {
    var isGranted by remember {
        mutableStateOf(
            AVAudioSession.sharedInstance().recordPermission == AVAudioSessionRecordPermissionGranted
        )
    }

    return PermissionState(
        isGranted = isGranted,
        launchRequest = {
            AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                isGranted = granted
            }
        }
    )
}

// ── Phone call ──────────────────────────────────────────────

@Composable
actual fun rememberPhoneCallPermission(): PermissionState {
    // iOS does not require a runtime permission for tel: URL scheme
    return PermissionState(isGranted = true, launchRequest = {})
}

// ── SMS ─────────────────────────────────────────────────────

@Composable
actual fun rememberSmsPermission(): PermissionState {
    // iOS does not require a runtime permission for MFMessageComposeViewController
    return PermissionState(isGranted = true, launchRequest = {})
}

// ── Notifications ───────────────────────────────────────────

@Composable
actual fun rememberNotificationPermission(): PermissionState {
    var isGranted by remember { mutableStateOf(false) }

    // Check current status on composition
    remember {
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                isGranted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
            }
        true
    }

    return PermissionState(
        isGranted = isGranted,
        launchRequest = {
            UNUserNotificationCenter.currentNotificationCenter()
                .requestAuthorizationWithOptions(
                    UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
                ) { granted, _ ->
                    isGranted = granted
                }
        }
    )
}
