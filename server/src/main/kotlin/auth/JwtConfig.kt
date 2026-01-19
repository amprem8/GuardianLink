package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    // Use environment variable for production secrets
    private val secret = System.getenv("JWT_SECRET") ?: "DUMMY_JWT_SECRET"
    private const val issuer = "com.guardianlink"
    private const val validityMs = 24 * 3600_000L // 24 hours
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(userId: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(now + validityMs))
            .sign(algorithm)
    }
}
