package model

import kotlinx.serialization.Serializable

/**
 * Represents a single emergency contact.
 *
 * @property id         Unique identifier (UUID string).
 * @property name       Display name of the contact.
 * @property phone      Indian mobile number formatted as "+91 XXXXXXXXXX".
 * @property includeGPS Whether GPS location should be shared in an SOS for this contact.
 * @property includeAudio Whether audio clip should be shared in an SOS for this contact.
 */
@Serializable
data class EmergencyContact(
    val id: String,
    val name: String,
    val phone: String,
    val includeGPS: Boolean = true,
    val includeAudio: Boolean = true,
)
