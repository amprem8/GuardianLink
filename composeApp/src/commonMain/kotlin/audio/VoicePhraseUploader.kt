package audio

/**
 * Cross-platform uploader that:
 * 1. Calls the backend POST /voice/presign to get a pre-signed S3 PUT URL
 * 2. HTTP-PUTs the local .m4a file directly to S3
 *
 * Returns the S3 key on success, throws on failure.
 */
expect class VoicePhraseUploader() {

    /**
     * @param backendBaseUrl  e.g. "https://api.resq.example.com"
     * @param username        Used as the S3 folder name
     * @param phrase          The voice phrase (used as sub-folder)
     * @param audioFilePath   Absolute local path to the recorded .m4a
     * @return                The S3 object key that was written
     */
    suspend fun upload(
        backendBaseUrl: String,
        username: String,
        phrase: String,
        audioFilePath: String,
    ): String
}

