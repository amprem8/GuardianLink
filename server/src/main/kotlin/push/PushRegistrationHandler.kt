package push

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.example.guardianlink.HttpResponses
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object PushRegistrationHandler {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Serializable
    data class RegisterPushRequest(
        val phoneNumber: String,
        val deviceId: String,
        val fcmToken: String,
        val platform: String = "ANDROID",
    )

    @Serializable
    data class RegisterPushResponse(
        val phoneNumber: String,
        val deviceId: String,
        val endpointArn: String,
        val platform: String,
        val registered: Boolean,
    )

    fun handle(event: APIGatewayV2HTTPEvent): APIGatewayV2HTTPResponse {
        return try {
            val body = event.body ?: return HttpResponses.badRequest("Missing request body")
            val req = json.decodeFromString(RegisterPushRequest.serializer(), body)

            val normalizedPhone = normalizePhone(req.phoneNumber)
            if (normalizedPhone.isBlank()) return HttpResponses.badRequest("phoneNumber is required")
            if (req.deviceId.isBlank()) return HttpResponses.badRequest("deviceId is required")
            if (req.fcmToken.isBlank()) return HttpResponses.badRequest("fcmToken is required")

            val existingEndpoint = runCatching {
                SnsEndpointRegistry.getEndpoint(normalizedPhone, req.deviceId)
            }.getOrNull()

            val endpointArn = if (!existingEndpoint.isNullOrBlank()) {
                runCatching {
                    SnsPushClient.upsertEndpointAttributes(existingEndpoint, req.fcmToken)
                    existingEndpoint
                }.getOrElse {
                    // Endpoint can become stale after app reinstall/token rotation.
                    SnsPushClient.createAndroidEndpoint(req.fcmToken)
                }
            } else {
                SnsPushClient.createAndroidEndpoint(req.fcmToken)
            }

            SnsEndpointRegistry.saveEndpoint(
                phone = normalizedPhone,
                deviceId = req.deviceId,
                endpointArn = endpointArn,
                platform = req.platform.ifBlank { "ANDROID" },
            )

            val response = RegisterPushResponse(
                phoneNumber = normalizedPhone,
                deviceId = req.deviceId,
                endpointArn = endpointArn,
                platform = req.platform.ifBlank { "ANDROID" },
                registered = true,
            )

            HttpResponses.ok(json.encodeToString(RegisterPushResponse.serializer(), response))
        } catch (e: Exception) {
            HttpResponses.internalError("Push registration failed: ${e::class.simpleName}")
        }
    }

    private fun normalizePhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return if (digits.length > 10) digits.takeLast(10) else digits
    }
}


