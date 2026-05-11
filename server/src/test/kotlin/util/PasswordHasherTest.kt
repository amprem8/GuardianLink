package util

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class PasswordHasherTest {

    @Test
    fun `hash returns a non-empty bcrypt hash`() {
        val hash = PasswordHasher.hash("MyPassword123")
        assertTrue(hash.isNotBlank())
        assertTrue(hash.startsWith("\$2a\$") || hash.startsWith("\$2b\$"))
    }

    @Test
    fun `hash produces different hashes for the same password`() {
        val hash1 = PasswordHasher.hash("SamePassword")
        val hash2 = PasswordHasher.hash("SamePassword")
        assertNotEquals(hash1, hash2, "BCrypt uses random salt so hashes must differ")
    }

    @Test
    fun `verify returns true for correct password`() {
        val password = "CorrectPassword123"
        val hash = PasswordHasher.hash(password)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `verify returns false for wrong password`() {
        val hash = PasswordHasher.hash("CorrectPassword")
        assertFalse(PasswordHasher.verify("WrongPassword", hash))
    }

    @Test
    fun `verify returns false for empty password against valid hash`() {
        val hash = PasswordHasher.hash("SomePassword")
        assertFalse(PasswordHasher.verify("", hash))
    }

    @Test
    fun `hash handles special characters`() {
        val password = "P@\$\$w0rd!#%^&*()"
        val hash = PasswordHasher.hash(password)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `hash handles very long password`() {
        val password = "a".repeat(200)
        val hash = PasswordHasher.hash(password)
        assertTrue(PasswordHasher.verify(password, hash))
    }

    @Test
    fun `hash handles unicode password`() {
        val password = "пароль密码パスワード"
        val hash = PasswordHasher.hash(password)
        assertTrue(PasswordHasher.verify(password, hash))
    }
}
