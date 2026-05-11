package permissions

import androidx.compose.runtime.Composable

/**
 * Reusable permission state that works the same way across all permission types.
 */
data class PermissionState(
    val isGranted: Boolean,
    val launchRequest: () -> Unit,
)

// ── Location ────────────────────────────────────────────────

/**
 * Returns a [PermissionState] for fine location access.
 * - Android: ACCESS_FINE_LOCATION
 * - iOS: CLLocationManager requestWhenInUseAuthorization
 */
@Composable
expect fun rememberLocationPermission(): PermissionState

// ── Microphone ──────────────────────────────────────────────

/**
 * Returns a [PermissionState] for microphone / audio recording access.
 * - Android: RECORD_AUDIO
 * - iOS: AVAudioSession requestRecordPermission
 */
@Composable
expect fun rememberMicrophonePermission(): PermissionState

// ── Phone call ──────────────────────────────────────────────

/**
 * Returns a [PermissionState] for making phone calls.
 * - Android: CALL_PHONE
 * - iOS: always granted (tel: URL scheme needs no runtime permission)
 */
@Composable
expect fun rememberPhoneCallPermission(): PermissionState

// ── SMS ─────────────────────────────────────────────────────

/**
 * Returns a [PermissionState] for sending SMS messages.
 * - Android: SEND_SMS
 * - iOS: always granted (MFMessageComposeViewController doesn't need runtime permission)
 */
@Composable
expect fun rememberSmsPermission(): PermissionState

// ── Notifications ───────────────────────────────────────────

/**
 * Returns a [PermissionState] for posting notifications.
 * - Android (13+): POST_NOTIFICATIONS
 * - iOS: UNUserNotificationCenter requestAuthorization
 */
@Composable
expect fun rememberNotificationPermission(): PermissionState
