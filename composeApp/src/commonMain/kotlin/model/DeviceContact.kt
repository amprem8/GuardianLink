package model

/**
 * Represents a contact fetched from the user's device address book.
 * Used when the user picks "From Contacts" mode.
 */
data class DeviceContact(
    val name: String,
    val phone: String,
)
