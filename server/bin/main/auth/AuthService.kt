package auth

import models.User
import db.DynamoClient
import models.AuthResponse
import models.LoginRequest
import models.SignupRequest
import util.PasswordHasher
import java.util.UUID

object AuthService {

    /**
     * Validates and creates a new user.
     * Throws [IllegalArgumentException] for validation failures.
     * Throws [IllegalStateException] if email already registered.
     */
    fun signup(req: SignupRequest): AuthResponse {
        // ── Validation ──
        require(req.name.isNotBlank()) { "Name is required" }
        require(req.email.isNotBlank()) { "Email is required" }
        require(req.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            "Please enter a valid email address"
        }
        require(req.password.length >= 8) { "Password must be at least 8 characters" }

        // ── Duplicate check ──
        val existing = DynamoClient.getUser(req.email)
        check(existing == null) { "An account with this email already exists" }

        // ── Create user ──
        val hashedPassword = PasswordHasher.hash(req.password)
        val user = User(
            userId = UUID.randomUUID().toString(),
            name = req.name.trim(),
            email = req.email.trim().lowercase(),
            passwordHash = hashedPassword,
            createdAt = System.currentTimeMillis()
        )
        DynamoClient.saveUser(user)

        val token = JwtConfig.generateToken(user.userId)
        return AuthResponse(user.userId, user.name, user.email, token)
    }

    fun login(req: LoginRequest): AuthResponse? {
        val user = DynamoClient.getUser(req.email) ?: return null
        if (!PasswordHasher.verify(req.password, user.passwordHash)) return null
        val token = JwtConfig.generateToken(user.userId)
        return AuthResponse(user.userId, user.name, user.email, token)
    }
}
