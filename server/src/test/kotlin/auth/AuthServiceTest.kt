package auth

import db.DynamoClient
import io.mockk.*
import models.SignupRequest
import models.LoginRequest
import models.User
import util.PasswordHasher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class AuthServiceTest {

    private fun setupMocks() {
        mockkObject(DynamoClient)
        mockkObject(JwtConfig)
        every { JwtConfig.generateToken(any()) } returns "mock-jwt-token"
    }

    private fun tearDownMocks() {
        unmockkAll()
    }

    // ── signup tests ──

    @Test
    fun `signup succeeds with valid input`() {
        setupMocks()
        try {
            every { DynamoClient.getUser(any()) } returns null
            every { DynamoClient.saveUser(any()) } just runs

            val req = SignupRequest(
                email = "test@example.com",
                password = "Password123",
                name = "Test User"
            )
            val result = AuthService.signup(req)

            assertNotNull(result)
            assertEquals("test@example.com", result.email)
            assertEquals("Test User", result.name)
            assertEquals("mock-jwt-token", result.token)
            verify { DynamoClient.saveUser(any()) }
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup normalizes email to lowercase`() {
        setupMocks()
        try {
            every { DynamoClient.getUser(any()) } returns null
            every { DynamoClient.saveUser(any()) } just runs

            val req = SignupRequest(
                email = "Test@Example.COM",
                password = "Password123",
                name = "Test"
            )
            val result = AuthService.signup(req)
            assertEquals("test@example.com", result.email)
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup trims name`() {
        setupMocks()
        try {
            every { DynamoClient.getUser(any()) } returns null
            every { DynamoClient.saveUser(any()) } just runs

            val req = SignupRequest(
                email = "test@example.com",
                password = "Password123",
                name = "  Spaced Name  "
            )
            val result = AuthService.signup(req)
            assertEquals("Spaced Name", result.name)
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup throws when name is blank`() {
        setupMocks()
        try {
            val req = SignupRequest(email = "a@b.com", password = "12345678", name = "")
            assertFailsWith<IllegalArgumentException>("Name is required") {
                AuthService.signup(req)
            }
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup throws when email is blank`() {
        setupMocks()
        try {
            val req = SignupRequest(email = "", password = "12345678", name = "Test")
            assertFailsWith<IllegalArgumentException>("Email is required") {
                AuthService.signup(req)
            }
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup throws for invalid email format`() {
        setupMocks()
        try {
            val req = SignupRequest(email = "not-an-email", password = "12345678", name = "Test")
            assertFailsWith<IllegalArgumentException> {
                AuthService.signup(req)
            }
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup throws when password is shorter than 8 characters`() {
        setupMocks()
        try {
            val req = SignupRequest(email = "test@example.com", password = "short", name = "Test")
            assertFailsWith<IllegalArgumentException> {
                AuthService.signup(req)
            }
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup throws when email already exists`() {
        setupMocks()
        try {
            val existingUser = User("id", "Name", "test@example.com", "hash", 0L)
            every { DynamoClient.getUser("test@example.com") } returns existingUser

            val req = SignupRequest(
                email = "test@example.com",
                password = "Password123",
                name = "Test"
            )
            assertFailsWith<IllegalStateException> {
                AuthService.signup(req)
            }
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup generates UUID for userId`() {
        setupMocks()
        try {
            every { DynamoClient.getUser(any()) } returns null
            val userSlot = slot<User>()
            every { DynamoClient.saveUser(capture(userSlot)) } just runs

            val req = SignupRequest(
                email = "test@example.com",
                password = "Password123",
                name = "Test"
            )
            AuthService.signup(req)

            val savedUser = userSlot.captured
            assertNotNull(savedUser.userId)
            assert(savedUser.userId.isNotBlank())
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `signup hashes password before saving`() {
        setupMocks()
        try {
            every { DynamoClient.getUser(any()) } returns null
            val userSlot = slot<User>()
            every { DynamoClient.saveUser(capture(userSlot)) } just runs

            val req = SignupRequest(
                email = "test@example.com",
                password = "PlainPassword123",
                name = "Test"
            )
            AuthService.signup(req)

            val savedUser = userSlot.captured
            // Password should be hashed, not plain text
            assert(savedUser.passwordHash != "PlainPassword123")
            assert(PasswordHasher.verify("PlainPassword123", savedUser.passwordHash))
        } finally {
            tearDownMocks()
        }
    }

    // ── login tests ──

    @Test
    fun `login returns response for valid credentials`() {
        setupMocks()
        try {
            val hashedPassword = PasswordHasher.hash("ValidPass123")
            val user = User("usr-1", "Test", "test@example.com", hashedPassword, 1000L)
            every { DynamoClient.getUser("test@example.com") } returns user

            val req = LoginRequest(email = "test@example.com", password = "ValidPass123")
            val result = AuthService.login(req)

            assertNotNull(result)
            assertEquals("usr-1", result.userId)
            assertEquals("Test", result.name)
            assertEquals("test@example.com", result.email)
            assertEquals("mock-jwt-token", result.token)
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `login returns null for non-existent user`() {
        setupMocks()
        try {
            every { DynamoClient.getUser("nobody@example.com") } returns null

            val req = LoginRequest(email = "nobody@example.com", password = "whatever")
            val result = AuthService.login(req)

            assertNull(result)
        } finally {
            tearDownMocks()
        }
    }

    @Test
    fun `login returns null for wrong password`() {
        setupMocks()
        try {
            val hashedPassword = PasswordHasher.hash("CorrectPassword")
            val user = User("usr-1", "Test", "test@example.com", hashedPassword, 1000L)
            every { DynamoClient.getUser("test@example.com") } returns user

            val req = LoginRequest(email = "test@example.com", password = "WrongPassword")
            val result = AuthService.login(req)

            assertNull(result)
        } finally {
            tearDownMocks()
        }
    }
}
