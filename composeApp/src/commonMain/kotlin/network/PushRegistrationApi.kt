package network

import config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

object PushRegistrationApi {

    private val client by lazy { createHttpClient() }

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

    suspend fun register(request: RegisterPushRequest): RegisterPushResponse {
        val response = client.post("${AppConfig.BASE_URL.trimEnd('/')}/push/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }
}

