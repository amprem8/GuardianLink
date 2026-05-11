@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package contacts

import model.DeviceContact

/**
 * Platform-specific helper that reads the user's address-book.
 *
 * On **Android** the implementation queries `ContactsContract`.
 * On **iOS** it uses `CNContactStore`.
 *
 * Must be initialised with a platform context before first use
 * (Android → `init(context)` from `MainActivity`; iOS → no-op).
 */
expect object DeviceContactsHelper {
    /**
     * Returns every contact that has at least one phone number.
     * Results are sorted alphabetically by display name.
     */
    suspend fun fetchContacts(): List<DeviceContact>
}
