package model

import kotlinx.serialization.Serializable

@Serializable
data class TriggerConfig(
    val voicePhrase: String = "Help Me",
    // "double-tap" | "triple-tap" | "shake" | "volume-triple-down"
    val gestureType: String = "double-tap",
    val useCustomPhrase: Boolean = false,
)
