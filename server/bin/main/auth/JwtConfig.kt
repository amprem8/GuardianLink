package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private val secret: String by lazy {
        requireNotNull(System.getenv("JWT_SECRET")) {
            "JWT_SECRET environment variable must be set"
        }
    }
    private const val ISSUER = "com.guardianlink"
    private const val VALIDITY_MS = 24 * 3600_000L // 24 hours
    private val algorithm: Algorithm by lazy { Algorithm.HMAC256(secret) }

    fun generateToken(userId: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withExpiresAt(Date(now + VALIDITY_MS))
            .sign(algorithm)
    }
}
