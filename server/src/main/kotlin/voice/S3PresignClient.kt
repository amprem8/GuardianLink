package voice

import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
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
        // DefaultCredentialsProvider automatically picks up:
        //   1. Lambda execution-role credentials (via AWS_CONTAINER_CREDENTIALS_RELATIVE_URI)
        //   2. Environment variables  (AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY)
        //   3. ~/.aws/credentials for local development
        S3Presigner.builder()
            .region(Region.AP_SOUTH_1)
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()
    }

    /**
     * Returns a Pair(presignedPutUrl, s3Key) valid for [URL_VALIDITY_MINUTES] minutes.
     * Single source of truth for key construction — callers never need to duplicate the
     * sanitise logic.
     */
    fun presignPutUrlWithKey(username: String, phrase: String): Pair<String, String> {
        val safeUsername = sanitise(username)
        val safePhrase   = sanitise(phrase)
        val key = "voice-phrases/$safeUsername/$safePhrase/audio.m4a"

        logger.info("Generating pre-signed PUT URL for key: {}", key)

        val putRequest = PutObjectRequest.builder()
            .bucket(BUCKET)
            .key(key)
            .contentType("audio/mp4")
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(URL_VALIDITY_MINUTES))
            .putObjectRequest(putRequest)
            .build()

        val url = presigner.presignPutObject(presignRequest).url().toString()
        logger.info("Pre-signed URL generated successfully for key: {}", key)
        return Pair(url, key)
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
