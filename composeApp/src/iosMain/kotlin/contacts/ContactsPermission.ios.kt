@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package contacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.Contacts.CNAuthorizationStatusAuthorized
import platform.Contacts.CNContactStore
import platform.Contacts.CNEntityType

@Composable
actual fun rememberContactsPermission(): ContactsPermissionState {
    var isGranted by remember {
        mutableStateOf(
            CNContactStore.authorizationStatusForEntityType(CNEntityType.CNEntityTypeContacts) ==
                    CNAuthorizationStatusAuthorized
        )
    }

    return ContactsPermissionState(
        isGranted = isGranted,
        launchRequest = {
            CNContactStore().requestAccessForEntityType(CNEntityType.CNEntityTypeContacts) { granted, _ ->
                isGranted = granted
            }
        }
    )
}
