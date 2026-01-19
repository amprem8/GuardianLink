package models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val userId: String,
    val name: String,
    val email: String,
    val token: String
)
