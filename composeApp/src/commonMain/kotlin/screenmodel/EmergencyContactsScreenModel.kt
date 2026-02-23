package screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import contacts.DeviceContactsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.DeviceContact
import model.EmergencyContact
import storage.ContactStorage
import kotlin.random.Random

private const val MIN_CONTACTS = 3
private const val MAX_CONTACTS = 10

class EmergencyContactsScreenModel : ScreenModel {

    // ── State ───────────────────────────────────────────────

    private val _contacts = MutableStateFlow(ContactStorage.loadContacts())
    val contacts = _contacts.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _deviceContacts = MutableStateFlow<List<DeviceContact>>(emptyList())
    val deviceContacts = _deviceContacts.asStateFlow()

    private val _isLoadingContacts = MutableStateFlow(false)
    val isLoadingContacts = _isLoadingContacts.asStateFlow()

    /** Cache: only fetch from device once per session. */
    private var contactsFetched = false

    val canContinue: Boolean get() = _contacts.value.size >= MIN_CONTACTS

    // ── Actions ─────────────────────────────────────────────

    fun setShowAddDialog(show: Boolean) {
        _showAddDialog.value = show
        if (!show) _error.value = ""
    }

    fun dismissError() { _error.value = "" }

    /** Add a contact from manual entry (validates Indian phone). */
    fun addContact(name: String, phone: String) {
        if (name.isBlank()) { _error.value = "Please enter contact name"; return }
        if (phone.isBlank()) { _error.value = "Please enter phone number"; return }
        if (!isValidIndianPhone(phone)) {
            _error.value = "Please enter a valid Indian mobile number (10 digits starting with 6-9)"
            return
        }
        if (_contacts.value.size >= MAX_CONTACTS) {
            _error.value = "Maximum $MAX_CONTACTS contacts allowed"
            return
        }
        // Check duplicate
        val formatted = formatIndianPhone(phone)
        if (_contacts.value.any { it.phone == formatted }) {
            _error.value = "This contact is already added"
            return
        }

        val contact = EmergencyContact(
            id = generateId(),
            name = name,
            phone = formatted,
            includeGPS = true,
            includeAudio = true,
        )
        _contacts.value = _contacts.value + contact
        persist()
        _showAddDialog.value = false
        _error.value = ""
    }

    /** Add a contact picked from the device address book. */
    fun addDeviceContact(dc: DeviceContact) {
        if (_contacts.value.size >= MAX_CONTACTS) {
            _error.value = "Maximum $MAX_CONTACTS contacts allowed"
            return
        }
        val cleanedPhone = dc.phone.replace("\\D".toRegex(), "")
        val formatted = formatIndianPhone(cleanedPhone)
        if (_contacts.value.any { it.phone == formatted }) {
            _error.value = "This contact is already added"
            return
        }
        val contact = EmergencyContact(
            id = generateId(),
            name = dc.name,
            phone = formatted,
            includeGPS = true,
            includeAudio = true,
        )
        _contacts.value = _contacts.value + contact
        persist()
        _error.value = ""
    }

    fun removeContact(id: String) {
        _contacts.value = _contacts.value.filter { it.id != id }
        persist()
    }

    fun toggleGPS(id: String) {
        _contacts.value = _contacts.value.map {
            if (it.id == id) it.copy(includeGPS = !it.includeGPS) else it
        }
        persist()
    }

    fun toggleAudio(id: String) {
        _contacts.value = _contacts.value.map {
            if (it.id == id) it.copy(includeAudio = !it.includeAudio) else it
        }
        persist()
    }

    fun loadDeviceContacts() {
        // Skip if already loaded (cached)
        if (contactsFetched && _deviceContacts.value.isNotEmpty()) return

        _isLoadingContacts.value = true
        screenModelScope.launch {
            try {
                _deviceContacts.value = DeviceContactsHelper.fetchContacts()
                contactsFetched = true
            } catch (_: Exception) {
                _deviceContacts.value = emptyList()
            } finally {
                _isLoadingContacts.value = false
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────

    private fun persist() { ContactStorage.saveContacts(_contacts.value) }

    companion object {
        /** Validates a 10-digit Indian mobile number (starting with 6-9). */
        fun isValidIndianPhone(phone: String): Boolean {
            val cleaned = phone.replace("\\D".toRegex(), "")
            // 10 digits starting with 6-9
            if (cleaned.length == 10 && cleaned[0] in '6'..'9') return true
            // With +91 prefix
            if (cleaned.length == 12 && cleaned.startsWith("91") && cleaned[2] in '6'..'9') return true
            return false
        }

        /** Normalises to "+91 XXXXXXXXXX" format. */
        fun formatIndianPhone(phone: String): String {
            val cleaned = phone.replace("\\D".toRegex(), "")
            return when {
                cleaned.length == 10 -> "+91 $cleaned"
                cleaned.length == 12 && cleaned.startsWith("91") ->
                    "+${cleaned.substring(0, 2)} ${cleaned.substring(2)}"
                else -> phone
            }
        }

        /** Simple random ID generator (no external library needed). */
        private fun generateId(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..24).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        }
    }
}
