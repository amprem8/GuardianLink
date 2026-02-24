package model

import kotlinx.serialization.Serializable

@Serializable
data class TriggerConfig(
    val voicePhrase: String = "Help Me",
    val gestureType: String = "double-tap", // "double-tap" or "shake"
    val useCustomPhrase: Boolean = false,
)
