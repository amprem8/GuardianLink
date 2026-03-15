package push

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.example.guardianlink.HttpResponses
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

object SosPushHandler {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private const val DUPLICATE_WINDOW_MS = 12_000L
    private val recentDispatches = LinkedHashMap<String, Long>()

    @Serializable
    data class SosPushRequest(
        val victimUserId: String,
        val victimName: String,
        val contacts: List<SosContactTarget>,
        val location: SosLocationContext? = null,
        val message: String? = null,
    )

    @Serializable
    data class SosContactTarget(
        val contactName: String,
        val phoneNumber: String? = null,
        val endpointArn: String = "",
        val includeGPS: Boolean = false,
    )

    @Serializable
    data class SosLocationContext(
        val permissionGranted: Boolean = false,
        val gpsEnabled: Boolean = false,
        val lat: Double? = null,
        val lng: Double? = null,
    )

    @Serializable
    data class ContactPublishResult(
        val contactName: String,
        val published: Boolean,
        val messageId: String? = null,
        val error: String? = null,
        val locationIncluded: Boolean = false,
        val deliveryMethod: String = "PUSH",
    )

    @Serializable
    data class SosPushResponse(
        val sosId: String,
        val sentCount: Int,
        val failedCount: Int,
        val allPublished: Boolean,
        val results: List<ContactPublishResult>,
    )

    private val phoneEndpointMap: Map<String, String> by lazy {
        val raw = System.getenv("SOS_PHONE_ENDPOINT_MAP_JSON").orEmpty().trim()
        if (raw.isEmpty()) return@lazy emptyMap()

        runCatching {
            json.parseToJsonElement(raw).jsonObject.mapNotNull { (k, v) ->
                val normalized = normalizePhone(k)
                val endpoint = runCatching { v.jsonPrimitive.content }.getOrDefault("").trim()
                if (normalized.isNotEmpty() && endpoint.isNotEmpty()) normalized to endpoint else null
            }.toMap()
        }.getOrDefault(emptyMap())
    }

    fun handle(event: APIGatewayV2HTTPEvent): APIGatewayV2HTTPResponse {
        return try {
            val body = event.body ?: return HttpResponses.badRequest("Missing request body")
            val req = json.decodeFromString(SosPushRequest.serializer(), body)

            if (req.victimUserId.isBlank()) return HttpResponses.badRequest("victimUserId is required")
            if (req.victimName.isBlank()) return HttpResponses.badRequest("victimName is required")
            if (req.contacts.isEmpty()) return HttpResponses.badRequest("contacts is required")

            val dedupeKey = buildString {
                append(req.victimUserId.trim())
                append('|')
                append(req.victimName.trim())
                append('|')
                append(req.message?.trim().orEmpty())
                append('|')
                append(req.contacts.size)
            }
            if (isDuplicateDispatch(dedupeKey)) {
                val skipped = req.contacts.map {
                    ContactPublishResult(
                        contactName = it.contactName,
                        published = true,
                        messageId = null,
                        error = null,
                        locationIncluded = false,
                        deliveryMethod = "SKIPPED_DUPLICATE",
                    )
                }
                return HttpResponses.ok(
                    json.encodeToString(
                        SosPushResponse(
                            sosId = "dedup-${System.currentTimeMillis()}",
                            sentCount = skipped.size,
                            failedCount = 0,
                            allPublished = true,
                            results = skipped,
                        )
                    )
                )
            }

            val sosId = UUID.randomUUID().toString()
            val helpText = "${req.victimName} might need help"
            val baseBody = req.message?.takeIf { it.isNotBlank() } ?: helpText
            val publishedByTarget = mutableMapOf<String, String>()

            val results = req.contacts.map { contact ->
                val resolvedEndpointArn = resolveEndpointArn(contact)
                val normalizedPhone = normalizePhone(contact.phoneNumber.orEmpty())
                val shouldIncludeLocation = contact.includeGPS &&
                        req.location?.permissionGranted == true &&
                        req.location.gpsEnabled &&
                        req.location.lat != null &&
                        req.location.lng != null

                val payload = mutableMapOf(
                    "type" to "SOS_ALERT",
                    "sosId" to sosId,
                    "victimUserId" to req.victimUserId,
                    "victimName" to req.victimName,
                    "title" to "SOS Alert",
                    "body" to baseBody,
                    "helpText" to helpText,
                )

                if (shouldIncludeLocation) {
                    payload["lat"] = req.location.lat.toString()
                    payload["lng"] = req.location.lng.toString()
                }

                val smsBody = buildString {
                    append(baseBody)
                    if (shouldIncludeLocation) {
                        append("\nLocation: https://maps.google.com/?q=")
                        append(req.location.lat)
                        append(",")
                        append(req.location.lng)
                    }
                }

                try {
                    if (resolvedEndpointArn.isNotBlank()) {
                        val targetKey = "PUSH:$resolvedEndpointArn"
                        val existingMessageId = publishedByTarget[targetKey]
                        if (existingMessageId != null) {
                            return@map ContactPublishResult(
                                contactName = contact.contactName,
                                published = true,
                                messageId = existingMessageId,
                                locationIncluded = shouldIncludeLocation,
                                deliveryMethod = "PUSH",
                            )
                        }

                        val messageId = runCatching {
                            SnsPushClient.publishSos(
                                endpointArn = resolvedEndpointArn,
                                title = "SOS Alert",
                                body = baseBody,
                                data = payload,
                            )
                        }.recoverCatching { firstError ->
                            // EndpointDisabledException — attempt to re-enable and retry once
                            val errName = firstError::class.simpleName.orEmpty()
                            if (errName.contains("EndpointDisabled", ignoreCase = true) ||
                                firstError.message?.contains("Endpoint is disabled", ignoreCase = true) == true
                            ) {
                                // Re-enable the endpoint in SNS (token update without new token is safe here)
                                runCatching {
                                    SnsPushClient.upsertEndpointAttributes(resolvedEndpointArn, resolvedEndpointArn)
                                }
                                // Retry publish
                                SnsPushClient.publishSos(
                                    endpointArn = resolvedEndpointArn,
                                    title = "SOS Alert",
                                    body = baseBody,
                                    data = payload,
                                )
                            } else {
                                throw firstError
                            }
                        }.getOrThrow()

                        publishedByTarget[targetKey] = messageId
                        return@map ContactPublishResult(
                            contactName = contact.contactName,
                            published = true,
                            messageId = messageId,
                            locationIncluded = shouldIncludeLocation,
                            deliveryMethod = "PUSH",
                        )
                    }

                    if (normalizedPhone.isBlank()) {
                        return@map ContactPublishResult(
                            contactName = contact.contactName,
                            published = false,
                            error = "Missing endpointArn/phone mapping",
                            locationIncluded = shouldIncludeLocation,
                            deliveryMethod = "NONE",
                        )
                    }

                    val targetKey = "SMS:$normalizedPhone"
                    val existingMessageId = publishedByTarget[targetKey]
                    if (existingMessageId != null) {
                        return@map ContactPublishResult(
                            contactName = contact.contactName,
                            published = true,
                            messageId = existingMessageId,
                            locationIncluded = shouldIncludeLocation,
                            deliveryMethod = "SMS",
                        )
                    }

                    val messageId = SnsPushClient.publishSms(
                        phoneNumberE164 = toE164(normalizedPhone),
                        body = smsBody,
                    )
                    publishedByTarget[targetKey] = messageId
                    ContactPublishResult(
                        contactName = contact.contactName,
                        published = true,
                        messageId = messageId,
                        locationIncluded = shouldIncludeLocation,
                        deliveryMethod = "SMS",
                    )
                } catch (e: Exception) {
                    ContactPublishResult(
                        contactName = contact.contactName,
                        published = false,
                        error = e::class.simpleName ?: "PublishError",
                        locationIncluded = shouldIncludeLocation,
                        deliveryMethod = if (resolvedEndpointArn.isNotBlank()) "PUSH" else "SMS",
                    )
                }
            }

            val sent = results.count { it.published }
            val failed = results.size - sent

            val resp = SosPushResponse(
                sosId = sosId,
                sentCount = sent,
                failedCount = failed,
                allPublished = failed == 0,
                results = results,
            )

            HttpResponses.ok(json.encodeToString(resp))
        } catch (e: Exception) {
            HttpResponses.internalError("SOS push failed: ${e::class.simpleName}")
        }
    }

    private fun resolveEndpointArn(contact: SosContactTarget): String {
        val direct = contact.endpointArn.trim()
        if (direct.isNotEmpty()) return direct

        val normalizedPhone = normalizePhone(contact.phoneNumber.orEmpty())
        if (normalizedPhone.isEmpty()) return ""

        return phoneEndpointMap[normalizedPhone].orEmpty()
    }

    private fun normalizePhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return if (digits.length > 10) digits.takeLast(10) else digits
    }

    private fun toE164(tenDigitPhone: String): String = "+91$tenDigitPhone"

    @Synchronized
    private fun isDuplicateDispatch(key: String): Boolean {
        val now = System.currentTimeMillis()
        val iterator = recentDispatches.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value > DUPLICATE_WINDOW_MS) iterator.remove()
        }
        val seenAt = recentDispatches[key]
        if (seenAt != null && now - seenAt <= DUPLICATE_WINDOW_MS) return true
        recentDispatches[key] = now
        return false
    }
}


