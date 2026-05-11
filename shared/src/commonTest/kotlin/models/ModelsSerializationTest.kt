package models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelsSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── AuthResponse ──

    @Test
    fun `AuthResponse serialization round-trip`() {
        val original = AuthResponse("usr-1", "John", "john@test.com", "jwt-token")
        val jsonStr = json.encodeToString(AuthResponse.serializer(), original)
        val restored = json.decodeFromString(AuthResponse.serializer(), jsonStr)
        assertEquals(original, restored)
    }

    @Test
    fun `AuthResponse deserialization from JSON`() {
        val jsonStr = """{"userId":"u1","name":"Alice","email":"alice@b.com","token":"tok"}"""
        val parsed = json.decodeFromString(AuthResponse.serializer(), jsonStr)
        assertEquals("u1", parsed.userId)
        assertEquals("Alice", parsed.name)
        assertEquals("alice@b.com", parsed.email)
        assertEquals("tok", parsed.token)
    }

    @Test
    fun `AuthResponse serialization contains expected fields`() {
        val response = AuthResponse("x", "Y", "z@test.com", "t")
        val jsonStr = json.encodeToString(AuthResponse.serializer(), response)
        assertTrue(jsonStr.contains("userId"))
        assertTrue(jsonStr.contains("name"))
        assertTrue(jsonStr.contains("email"))
        assertTrue(jsonStr.contains("token"))
    }

    // ── LoginRequest ──

    @Test
    fun `LoginRequest serialization round-trip`() {
        val original = LoginRequest("user@test.com", "password123")
        val jsonStr = json.encodeToString(LoginRequest.serializer(), original)
        val restored = json.decodeFromString(LoginRequest.serializer(), jsonStr)
        assertEquals(original, restored)
    }

    @Test
    fun `LoginRequest deserialization from JSON`() {
        val jsonStr = """{"email":"a@b.com","password":"secret"}"""
        val parsed = json.decodeFromString(LoginRequest.serializer(), jsonStr)
        assertEquals("a@b.com", parsed.email)
        assertEquals("secret", parsed.password)
    }

    // ── SignupRequest ──

    @Test
    fun `SignupRequest serialization round-trip`() {
        val original = SignupRequest("user@test.com", "password123", "UserName")
        val jsonStr = json.encodeToString(SignupRequest.serializer(), original)
        val restored = json.decodeFromString(SignupRequest.serializer(), jsonStr)
        assertEquals(original, restored)
    }

    @Test
    fun `SignupRequest deserialization from JSON`() {
        val jsonStr = """{"email":"a@b.com","password":"pass","name":"Name"}"""
        val parsed = json.decodeFromString(SignupRequest.serializer(), jsonStr)
        assertEquals("a@b.com", parsed.email)
        assertEquals("pass", parsed.password)
        assertEquals("Name", parsed.name)
    }

    @Test
    fun `SignupRequest fields are accessible`() {
        val request = SignupRequest("e@mail.com", "pwd12345", "Full Name")
        assertEquals("e@mail.com", request.email)
        assertEquals("pwd12345", request.password)
        assertEquals("Full Name", request.name)
    }
}
