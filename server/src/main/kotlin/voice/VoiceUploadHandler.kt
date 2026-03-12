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

            // presignPutUrl returns the URL AND internally builds the key — retrieve both via the
            // dedicated method so we never duplicate the sanitise logic.
            val (uploadUrl, s3Key) = S3PresignClient.presignPutUrlWithKey(req.username, req.phrase)

            val responseBody = json.encodeToString(
                PresignResponse.serializer(),
                PresignResponse(uploadUrl = uploadUrl, s3Key = s3Key)
            )
            HttpResponses.ok(responseBody)

        } catch (e: Exception) {
            // Log the real cause so it appears in CloudWatch Logs
            System.err.println("VoiceUploadHandler error: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            HttpResponses.internalError("Presign failed: ${e::class.simpleName} – ${e.message?.take(200)}")
        }
    }
}

