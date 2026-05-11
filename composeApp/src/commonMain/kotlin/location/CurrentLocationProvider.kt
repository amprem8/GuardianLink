package location

/** Best-effort current location used for SOS payloads. */
data class CurrentLocation(
    val latitude: Double,
    val longitude: Double,
)

expect suspend fun getCurrentLocationOrNull(): CurrentLocation?

