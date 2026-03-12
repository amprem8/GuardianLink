package network

import config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

object SosPushApi {

    private val client by lazy { createHttpClient() }

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
        return response.body()
    }
}

