package voice

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.example.guardianlink.HttpResponses
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Handles POST /voice/presign
 *
 * Request body:
 * {
 *   "username": "john_doe",
 *   "phrase": "Help Me"
 * }
 *
 * Response (200):
 * {
 *   "uploadUrl": "https://resq-voice-phrases.s3.amazonaws.com/voice-phrases/john_doe/help_me/audio.m4a?X-Amz-..."
 *   "s3Key": "voice-phrases/john_doe/help_me/audio.m4a"
 * }
 */
object VoiceUploadHandler {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class PresignRequest(val username: String, val phrase: String)

    @Serializable
    data class PresignResponse(val uploadUrl: String, val s3Key: String)

    fun handlePresign(event: APIGatewayV2HTTPEvent): APIGatewayV2HTTPResponse {
        return try {
            val body = event.body ?: return HttpResponses.badRequest("Missing request body")
            val req = json.decodeFromString<PresignRequest>(body)

            if (req.username.isBlank()) return HttpResponses.badRequest("username is required")
            if (req.phrase.isBlank())   return HttpResponses.badRequest("phrase is required")

            val uploadUrl = S3PresignClient.presignPutUrl(req.username, req.phrase)

            // Reconstruct the key the same way S3PresignClient does (mirror sanitise logic)
            val safeUsername = req.username.trim().replace(" ", "_")
                .replace(Regex("[^A-Za-z0-9_\\-]"), "").lowercase().take(64).ifEmpty { "unknown" }
            val safePhrase = req.phrase.trim().replace(" ", "_")
                .replace(Regex("[^A-Za-z0-9_\\-]"), "").lowercase().take(64).ifEmpty { "unknown" }
            val s3Key = "voice-phrases/$safeUsername/$safePhrase/audio.m4a"

            val responseBody = json.encodeToString(
                PresignResponse.serializer(),
                PresignResponse(uploadUrl = uploadUrl, s3Key = s3Key)
            )
            HttpResponses.ok(responseBody)

        } catch (e: Exception) {
            HttpResponses.internalError()
        }
    }
}

