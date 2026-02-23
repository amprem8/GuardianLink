@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import model.EmergencyContact

/**
 * Securely persists the user's emergency-contact list.
 *
 * - **Android**: AES-256 encrypted SharedPreferences (`EncryptedSharedPreferences`).
 * - **iOS**: `NSUserDefaults` (sandboxed + hardware-level full-disk encryption).
 *
 * Data is serialised as a JSON array and stored under a single key.
 */
expect object ContactStorage {
    fun saveContacts(contacts: List<EmergencyContact>)
    fun loadContacts(): List<EmergencyContact>
    fun clearContacts()
}
