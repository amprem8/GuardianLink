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

object PushRegistrationApi {

    private val client by lazy { createHttpClient() }
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class ApiErrorResponse(
        val message: String? = null,
        val error: String? = null,
    )

    @Serializable
    data class RegisterPushRequest(
        val phoneNumber: String,
        val deviceId: String,
        val fcmToken: String,
        val platform: String = "ANDROID",
    )

    @Serializable
    data class RegisterPushResponse(
        val phoneNumber: String = "",
        val deviceId: String = "",
        val endpointArn: String = "",
        val platform: String = "ANDROID",
        val registered: Boolean = false,
    )

    suspend fun register(request: RegisterPushRequest): RegisterPushResponse {
        val response = client.post("${AppConfig.BASE_URL.trimEnd('/')}/push/register") {
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
            json.decodeFromString(RegisterPushResponse.serializer(), responseText)
        }.getOrElse {
            throw IllegalStateException(
                "Unexpected /push/register response: ${responseText.take(220)}",
                it,
            )
        }
    }
}

