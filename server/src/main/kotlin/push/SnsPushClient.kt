package push

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointRequest
import software.amazon.awssdk.services.sns.model.GetEndpointAttributesRequest
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.SetEndpointAttributesRequest
import software.amazon.awssdk.services.sns.model.SnsException

object SnsPushClient {
    private val androidPlatformApplicationArn: String by lazy {
        System.getenv("SNS_ANDROID_PLATFORM_APP_ARN")
            .orEmpty()
            .trim()
            .ifEmpty {
                throw IllegalStateException("SNS_ANDROID_PLATFORM_APP_ARN is not configured")
            }
    }


    private val json = Json { encodeDefaults = true }

    private val client: SnsClient by lazy {
        SnsClient.builder()
            .region(Region.AP_SOUTH_1)
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()
    }

    /**
     * Publishes a mobile push payload to a specific SNS endpoint ARN.
     * Returns SNS messageId on success.
     */
    fun publishSos(
        endpointArn: String,
        title: String,
        body: String,
        data: Map<String, String>,
    ): String {
        val gcm = buildJsonObject {
            put("priority", "high")
            putJsonObject("data") {
                data.forEach { (k, v) -> put(k, v) }
                put("title", title)
                put("body", body)
            }
        }

        val apns = buildJsonObject {
            put("aps", buildJsonObject {
                put("alert", buildJsonObject {
                    put("title", title)
                    put("body", body)
                })
                put("sound", "default")
                put("content-available", 1)
            })
            putJsonObject("data") {
                data.forEach { (k, v) -> put(k, v) }
            }
        }

        val messageStructure = buildJsonObject {
            put("default", body)
            put("GCM", json.encodeToString(gcm))
            put("APNS", json.encodeToString(apns))
            put("APNS_SANDBOX", json.encodeToString(apns))
        }

        val req = PublishRequest.builder()
            .targetArn(endpointArn)
            .messageStructure("json")
            .message(json.encodeToString(messageStructure))
            .build()

        return client.publish(req).messageId()
    }

    fun createAndroidEndpoint(fcmToken: String): String {
        val token = fcmToken.trim()
        require(token.isNotEmpty()) { "fcmToken is required" }

        val req = CreatePlatformEndpointRequest.builder()
            .platformApplicationArn(androidPlatformApplicationArn)
            .token(token)
            .build()

        return try {
            client.createPlatformEndpoint(req).endpointArn()
        } catch (e: SnsException) {
            val existingArn = extractExistingEndpointArn(e.message)
            if (existingArn != null) {
                upsertEndpointAttributes(existingArn, token)
                existingArn
            } else {
                throw e
            }
        }
    }

    fun upsertEndpointAttributes(endpointArn: String, fcmToken: String) {
        val arn = endpointArn.trim()
        val token = fcmToken.trim()
        require(arn.isNotEmpty()) { "endpointArn is required" }
        require(token.isNotEmpty()) { "fcmToken is required" }

        val req = SetEndpointAttributesRequest.builder()
            .endpointArn(arn)
            .attributes(
                mapOf(
                    "Token" to token,
                    "Enabled" to "true",   // re-enable if SNS disabled it after token rotation
                )
            )
            .build()

        client.setEndpointAttributes(req)
    }

    /**
     * Checks whether an SNS endpoint is disabled and re-enables it with the supplied token.
     * Returns true if the endpoint was found and is now enabled.
     */
    fun reenableEndpointIfDisabled(endpointArn: String, fcmToken: String): Boolean {
        return runCatching {
            val attrs = client.getEndpointAttributes(
                GetEndpointAttributesRequest.builder().endpointArn(endpointArn).build()
            ).attributes()
            val isEnabled = attrs["Enabled"]?.equals("true", ignoreCase = true) ?: false
            val storedToken = attrs["Token"].orEmpty()
            if (!isEnabled || storedToken != fcmToken) {
                upsertEndpointAttributes(endpointArn, fcmToken)
            }
            true
        }.getOrElse { false }
    }

    /**
     * SMS fallback for emergency contacts who do not have app push endpoint registered.
     */
    fun publishSms(phoneNumberE164: String, body: String): String {
        val req = PublishRequest.builder()
            .phoneNumber(phoneNumberE164)
            .message(body)
            .build()

        return client.publish(req).messageId()
    }

    private fun extractExistingEndpointArn(message: String?): String? {
        if (message.isNullOrBlank()) return null
        val marker = "Endpoint "
        val start = message.indexOf(marker)
        if (start < 0) return null
        val from = start + marker.length
        val end = message.indexOf(" already exists", from)
        if (end <= from) return null
        val arn = message.substring(from, end).trim()
        return arn.ifEmpty { null }
    }
}


