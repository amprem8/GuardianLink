package screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import contacts.rememberContactsPermission
import screenmodel.EmergencyContactsScreenModel
import storage.AppStorage
import ui.EmergencyContactsScreen

class EmergencyContactsActivity(
    private val isSetupFlow: Boolean = true
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val model = rememberScreenModel { EmergencyContactsScreenModel() }

        val contacts by model.contacts.collectAsState()
        val showAddDialog by model.showAddDialog.collectAsState()
        val error by model.error.collectAsState()
        val deviceContacts by model.deviceContacts.collectAsState()
        val isLoadingContacts by model.isLoadingContacts.collectAsState()

        val permissionState = rememberContactsPermission()

        EmergencyContactsScreen(
            contacts = contacts,
            error = error,
            showAddDialog = showAddDialog,
            deviceContacts = deviceContacts,
            isLoadingContacts = isLoadingContacts,
            permissionState = permissionState,
            verifiedPhone = AppStorage.getPhoneNumber(),
            onAddContact = model::addContact,
            onAddDeviceContact = model::addDeviceContact,
            onRemoveContact = model::removeContact,
            onToggleGPS = model::toggleGPS,
            onToggleAudio = model::toggleAudio,
            onShowAddDialog = model::setShowAddDialog,
            onDismissError = model::dismissError,
            onLoadDeviceContacts = model::loadDeviceContacts,
            onContinue = {
                if (model.canContinue) {
                    AppStorage.markContactsConfigured()
                    if (isSetupFlow) {
                        navigator?.push(TriggerConfigActivity())
                    } else {
                        navigator?.pop()
                    }
                }
            },
            onBack = { navigator?.pop() },
        )
    }
}
