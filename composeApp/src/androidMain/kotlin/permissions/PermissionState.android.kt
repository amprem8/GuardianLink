package permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// ── Helper ──────────────────────────────────────────────────

@Composable
private fun rememberSinglePermission(permission: String): PermissionState {
    val context = LocalContext.current
    var isGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> isGranted = granted }

    return PermissionState(
        isGranted = isGranted,
        launchRequest = { launcher.launch(permission) }
    )
}

// ── Location ────────────────────────────────────────────────

@Composable
actual fun rememberLocationPermission(): PermissionState =
    rememberSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION)

// ── Microphone ──────────────────────────────────────────────

@Composable
actual fun rememberMicrophonePermission(): PermissionState =
    rememberSinglePermission(Manifest.permission.RECORD_AUDIO)

// ── Phone call ──────────────────────────────────────────────

@Composable
actual fun rememberPhoneCallPermission(): PermissionState =
    rememberSinglePermission(Manifest.permission.CALL_PHONE)

// ── SMS ─────────────────────────────────────────────────────

@Composable
actual fun rememberSmsPermission(): PermissionState =
    rememberSinglePermission(Manifest.permission.SEND_SMS)

// ── Notifications ───────────────────────────────────────────

@Composable
actual fun rememberNotificationPermission(): PermissionState {
    // POST_NOTIFICATIONS only exists on Android 13+ (API 33)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return PermissionState(isGranted = true, launchRequest = {})
    }
    return rememberSinglePermission(Manifest.permission.POST_NOTIFICATIONS)
}
