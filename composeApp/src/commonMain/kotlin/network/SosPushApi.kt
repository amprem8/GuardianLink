package network

import config.AppConfig
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object SosPushApi {

    private val client by lazy { createHttpClient() }
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class ApiErrorResponse(
        val message: String? = null,
        val error: String? = null,
    )

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

    suspend fun trigger(request: SosPushRequest): SosPushResponse {
        val response = client.post("${AppConfig.BASE_URL.trimEnd('/')}/sos/push") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val responseText = response.bodyAsText()

        if (!response.status.isSuccess()) {
            val parsed = runCatching {
                json.decodeFromString(ApiErrorResponse.serializer(), responseText)
            }.getOrNull()

            val message = parsed?.message
                ?: parsed?.error
                ?: responseText.take(220).ifBlank { "HTTP ${response.status.value}" }

            throw IllegalStateException(message)
        }

        return runCatching {
            json.decodeFromString(SosPushResponse.serializer(), responseText)
        }.getOrElse {
            throw IllegalStateException(
                "Unexpected /sos/push response: ${responseText.take(220)}",
                it,
            )
        }
    }
}

