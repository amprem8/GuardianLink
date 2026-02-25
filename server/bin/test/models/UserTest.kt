package models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class UserTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `User can be serialized to JSON`() {
        val user = User(
            userId = "usr-001",
            name = "John Doe",
            email = "john@example.com",
            passwordHash = "\$2a\$10\$hash",
            createdAt = 1700000000000L
        )
        val jsonStr = json.encodeToString(User.serializer(), user)
        assertTrue(jsonStr.contains("usr-001"))
        assertTrue(jsonStr.contains("john@example.com"))
    }

    @Test
    fun `User can be deserialized from JSON`() {
        val jsonStr = """
            {
                "userId": "usr-002",
                "name": "Jane Smith",
                "email": "jane@example.com",
                "passwordHash": "hash123",
                "createdAt": 1700000000000
            }
        """.trimIndent()
        val user = json.decodeFromString(User.serializer(), jsonStr)
        assertEquals("usr-002", user.userId)
        assertEquals("Jane Smith", user.name)
        assertEquals("jane@example.com", user.email)
        assertEquals("hash123", user.passwordHash)
        assertEquals(1700000000000L, user.createdAt)
    }

    @Test
    fun `User serialization round-trip preserves all fields`() {
        val original = User(
            userId = "usr-003",
            name = "Test User",
            email = "test@test.com",
            passwordHash = "bcrypt-hash-value",
            createdAt = 1234567890L
        )
        val jsonStr = json.encodeToString(User.serializer(), original)
        val restored = json.decodeFromString(User.serializer(), jsonStr)
        assertEquals(original, restored)
    }

    @Test
    fun `User data class equals works correctly`() {
        val user1 = User("id1", "Name", "email@test.com", "hash", 100L)
        val user2 = User("id1", "Name", "email@test.com", "hash", 100L)
        assertEquals(user1, user2)
    }

    @Test
    fun `User data class copy works`() {
        val user = User("id1", "Name", "email@test.com", "hash", 100L)
        val updated = user.copy(name = "Updated Name")
        assertEquals("Updated Name", updated.name)
        assertEquals("id1", updated.userId)
    }

    private fun assertTrue(condition: Boolean) {
        kotlin.test.assertTrue(condition)
    }
}
