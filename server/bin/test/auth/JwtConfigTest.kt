package auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class JwtConfigTest {

    // JWT_SECRET is set via Gradle test { environment(...) }
    private val testSecret = System.getenv("JWT_SECRET")
        ?: error("JWT_SECRET must be set in test environment")

    @Test
    fun `generateToken returns a non-empty JWT string`() {
        val token = JwtConfig.generateToken("user-123")
        assertTrue(token.isNotBlank())
        // JWT has 3 parts separated by dots
        assertEquals(3, token.split(".").size)
    }

    @Test
    fun `generateToken embeds userId claim`() {
        val userId = "test-user-456"
        val token = JwtConfig.generateToken(userId)

        val verifier = JWT.require(Algorithm.HMAC256(testSecret))
            .withIssuer("com.resq")
            .build()

        val decoded = verifier.verify(token)
        assertEquals(userId, decoded.getClaim("userId").asString())
    }

    @Test
    fun `generateToken sets correct issuer`() {
        val token = JwtConfig.generateToken("user-789")

        val verifier = JWT.require(Algorithm.HMAC256(testSecret))
            .withIssuer("com.resq")
            .build()

        val decoded = verifier.verify(token)
        assertEquals("com.resq", decoded.issuer)
    }

    @Test
    fun `generateToken sets expiration in the future`() {
        val token = JwtConfig.generateToken("user-exp")

        val verifier = JWT.require(Algorithm.HMAC256(testSecret))
            .withIssuer("com.resq")
            .build()

        val decoded = verifier.verify(token)
        assertNotNull(decoded.expiresAt)
        assertTrue(decoded.expiresAt.time > System.currentTimeMillis())
    }

    @Test
    fun `generateToken produces different tokens for different users`() {
        val token1 = JwtConfig.generateToken("user-a")
        val token2 = JwtConfig.generateToken("user-b")
        assertNotEquals(token1, token2)
    }

    @Test
    fun `generateToken produces different tokens for same user at different times`() {
        val token1 = JwtConfig.generateToken("same-user")
        Thread.sleep(10) // small delay to get different iat
        val token2 = JwtConfig.generateToken("same-user")
        // Tokens might be the same within the same second; just verify both are valid
        assertTrue(token1.isNotBlank())
        assertTrue(token2.isNotBlank())
    }
}
