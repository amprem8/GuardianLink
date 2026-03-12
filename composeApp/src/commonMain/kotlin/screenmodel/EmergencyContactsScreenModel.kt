package screenmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import contacts.DeviceContactsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.DeviceContact
import model.EmergencyContact
import storage.AppStorage
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

    // Verified phone (last 10 digits) — used to block user's own number
    private val verifiedDigits: String
        get() = AppStorage.getPhoneNumber().replace("\\D".toRegex(), "").takeLast(10)

    // ── Actions ─────────────────────────────────────────────

    fun setShowAddDialog(show: Boolean) {
        _showAddDialog.value = show
        if (!show) _error.value = ""
    }

    fun dismissError() { _error.value = "" }

    /** Add a contact from manual entry (validates Indian phone). */
    fun addContact(name: String, phone: String) {
        if (name.isBlank()) { _error.value = "Please enter contact name"; return }
        if (name.length > 10) { _error.value = "Name must be 10 characters or less"; return }
        if (phone.isBlank()) { _error.value = "Please enter phone number"; return }
        val cleanedPhone = phone.replace("\\D".toRegex(), "")
        if (cleanedPhone.length != 10) {
            _error.value = "Phone number must be exactly 10 digits"
            return
        }
        if (!isValidIndianPhone(cleanedPhone)) {
            _error.value = "Please enter a valid Indian mobile number (10 digits starting with 6-9)"
            return
        }
        val vd = verifiedDigits
        if (vd.isNotEmpty() && cleanedPhone.takeLast(10) == vd) {
            _error.value = "Your verified number cannot be added as an emergency contact"
            return
        }
        if (_contacts.value.size >= MAX_CONTACTS) {
            _error.value = "Maximum $MAX_CONTACTS contacts allowed"
            return
        }
        val formatted = formatIndianPhone(cleanedPhone)
        if (_contacts.value.any { normalizeToDigits(it.phone) == normalizeToDigits(formatted) }) {
            _error.value = "This phone number is already added"
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
        val last10 = cleanedPhone.takeLast(10)
        if (!isValidIndianPhone(cleanedPhone)) {
            _error.value = "Only Indian mobile numbers are accepted (10 digits starting with 6-9)"
            return
        }
        val vd = verifiedDigits
        if (vd.isNotEmpty() && last10 == vd) {
            _error.value = "Your verified number cannot be added as an emergency contact"
            return
        }
        // Truncate name if device contact name is longer than 10 chars
        val safeName = dc.name.take(10)
        val formatted = formatIndianPhone(cleanedPhone)
        if (_contacts.value.any { normalizeToDigits(it.phone) == normalizeToDigits(formatted) }) {
            _error.value = "This phone number is already added"
            return
        }
        val contact = EmergencyContact(
            id = generateId(),
            name = safeName,
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
        if (contactsFetched) return

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

        /** Strips all non-digit characters for comparison. */
        fun normalizeToDigits(phone: String): String {
            val digits = phone.replace("\\D".toRegex(), "")
            // Normalize to last 10 digits for consistent comparison
            return if (digits.length >= 10) digits.takeLast(10) else digits
        }

        /** Simple random ID generator (no external library needed). */
        private fun generateId(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..24).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        }
    }
}
