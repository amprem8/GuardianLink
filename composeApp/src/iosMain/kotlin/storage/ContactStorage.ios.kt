@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.EmergencyContact
import platform.Foundation.NSUserDefaults

actual object ContactStorage {

    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveContacts(contacts: List<EmergencyContact>) {
        val encoded = json.encodeToString(contacts)
        defaults.setObject(encoded, forKey = KEY_CONTACTS)
        defaults.synchronize()
    }

    actual fun loadContacts(): List<EmergencyContact> {
        val raw = defaults.stringForKey(KEY_CONTACTS) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    actual fun clearContacts() {
        defaults.removeObjectForKey(KEY_CONTACTS)
        defaults.synchronize()
    }

    private const val KEY_CONTACTS = "emergency_contacts"
}
