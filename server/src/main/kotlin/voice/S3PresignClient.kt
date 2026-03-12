package voice

import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

object S3PresignClient {

    private val logger = LoggerFactory.getLogger(S3PresignClient::class.java)

    private const val BUCKET = "resq-voice-phrases"
    private const val URL_VALIDITY_MINUTES = 10L

    private val presigner: S3Presigner by lazy {
        S3Presigner.builder()
            .region(Region.AP_SOUTH_1)
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .build()
    }

    /**
     * Returns a pre-signed PUT URL valid for [URL_VALIDITY_MINUTES] minutes.
     *
     * @param username  The user's name/id (used as folder)
     * @param phrase    The activation phrase (sanitised, used as sub-folder)
     * @return          HTTPS pre-signed URL string
     */
    fun presignPutUrl(username: String, phrase: String): String {
        val safeUsername = sanitise(username)
        val safePhrase   = sanitise(phrase)
        val key = "voice-phrases/$safeUsername/$safePhrase/audio.m4a"

        logger.info("Generating pre-sign PUT URL for key: {}", key)

        val putRequest = PutObjectRequest.builder()
            .bucket(BUCKET)
            .key(key)
            .contentType("audio/mp4")
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(URL_VALIDITY_MINUTES))
            .putObjectRequest(putRequest)
            .build()

        return presigner.presignPutObject(presignRequest).url().toString()
    }

    /** Replace spaces with underscores and strip everything except alphanumerics / hyphens / underscores. */
    private fun sanitise(raw: String): String =
        raw.trim()
            .replace(" ", "_")
            .replace(Regex("[^A-Za-z0-9_\\-]"), "")
            .lowercase()
            .take(64)
            .ifEmpty { "unknown" }
}
