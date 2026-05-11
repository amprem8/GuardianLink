@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package contacts

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.DeviceContact
import platform.Contacts.CNContactFetchRequest
import platform.Contacts.CNContactFamilyNameKey
import platform.Contacts.CNContactGivenNameKey
import platform.Contacts.CNContactPhoneNumbersKey
import platform.Contacts.CNContactStore
import platform.Contacts.CNLabeledValue
import platform.Contacts.CNPhoneNumber

actual object DeviceContactsHelper {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun fetchContacts(): List<DeviceContact> = withContext(Dispatchers.Default) {
        val result = mutableListOf<DeviceContact>()
        val store = CNContactStore()

        val keysToFetch = listOf(
            CNContactGivenNameKey,
            CNContactFamilyNameKey,
            CNContactPhoneNumbersKey,
        )

        val request = CNContactFetchRequest(keysToFetch = keysToFetch)

        try {
            store.enumerateContactsWithFetchRequest(request, error = null) { contact, _ ->
                val givenName = contact?.givenName.orEmpty()
                val familyName = contact?.familyName.orEmpty()
                val displayName = "$givenName $familyName".trim()

                @Suppress("UNCHECKED_CAST")
                val phones = (contact?.phoneNumbers as? List<CNLabeledValue>).orEmpty()
                for (labeled in phones) {
                    val number = (labeled.value as? CNPhoneNumber)?.stringValue.orEmpty()
                    if (number.isNotBlank() && displayName.isNotBlank()) {
                        result.add(
                            DeviceContact(
                                name = displayName,
                                phone = number.replace("\\s".toRegex(), ""),
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
            // Permission denied or contact store unavailable
        }

        result.distinctBy { "${it.name}|${it.phone}" }
            .sortedBy { it.name.lowercase() }
    }
}
