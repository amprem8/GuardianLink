package push

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest

object SnsPushClient {

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
            put("notification", buildJsonObject {
                put("title", title)
                put("body", body)
                put("sound", "default")
            })
            putJsonObject("data") {
                data.forEach { (k, v) -> put(k, v) }
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
}


