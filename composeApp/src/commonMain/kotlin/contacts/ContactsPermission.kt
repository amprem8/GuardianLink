@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package contacts

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic snapshot of the READ_CONTACTS permission status.
 *
 * @property isGranted    `true` when the user has already granted contact access.
 * @property launchRequest  Invoke to show the system permission dialog.
 */
data class ContactsPermissionState(
    val isGranted: Boolean,
    val launchRequest: () -> Unit,
)

/**
 * Returns a [ContactsPermissionState] that reflects the current runtime permission
 * for reading the device address-book.
 *
 * - **Android**: uses `ActivityResultContracts.RequestPermission()` under the hood.
 * - **iOS**: uses `CNContactStore.requestAccessForEntityType`.
 */
@Composable
expect fun rememberContactsPermission(): ContactsPermissionState
