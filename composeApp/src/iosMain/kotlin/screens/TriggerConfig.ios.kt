package screens

import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

actual fun tempAudioFilePath(): String {
    val uuid = NSUUID.UUID().UUIDString()
    return "${NSTemporaryDirectory()}voice_phrase_$uuid.m4a"
}

