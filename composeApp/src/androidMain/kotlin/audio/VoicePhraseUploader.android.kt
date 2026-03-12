package audio

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

actual class VoicePhraseUploader actual constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    @Serializable
    private data class PresignRequest(val username: String, val phrase: String)

    @Serializable
    private data class PresignResponse(val uploadUrl: String, val s3Key: String)

    actual suspend fun upload(
        backendBaseUrl: String,
        username: String,
        phrase: String,
        audioFilePath: String,
    ): String {
        // ── Step 1: Get pre-signed PUT URL from backend ──────────────
        val presignResponse = client.post("$backendBaseUrl/voice/presign") {
            contentType(ContentType.Application.Json)
            setBody(PresignRequest(username = username, phrase = phrase))
        }

        if (!presignResponse.status.isSuccess()) {
            throw Exception("Presign request failed: HTTP ${presignResponse.status.value}")
        }

        val presign = json.decodeFromString<PresignResponse>(presignResponse.bodyAsText())

        // ── Step 2: PUT audio file directly to S3 ───────────────────
        val audioFile = File(audioFilePath)
        if (!audioFile.exists()) throw Exception("Audio file not found: $audioFilePath")

        val audioBytes = audioFile.readBytes()

        val s3Response = client.put(presign.uploadUrl) {
            header(HttpHeaders.ContentType, "audio/mp4")
            setBody(audioBytes)
        }

        if (!s3Response.status.isSuccess()) {
            throw Exception("S3 upload failed: HTTP ${s3Response.status.value}")
        }

        return presign.s3Key
    }
}

