package audio

actual class VoicePhraseUploader actual constructor() {

    actual suspend fun upload(
        backendBaseUrl: String,
        username: String,
        phrase: String,
        audioFilePath: String,
    ): String {
        // iOS implementation: URLSession — stubbed
        throw NotImplementedError("iOS voice upload not yet implemented")
    }
}

