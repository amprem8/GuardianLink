package session

import models.AuthResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserSessionTest {

    private fun resetSession() {
        UserSession.logout()
    }

    @Test
    fun `initially not logged in`() {
        resetSession()
        assertFalse(UserSession.isLoggedIn)
        assertNull(UserSession.currentUser)
    }

    @Test
    fun `login sets currentUser`() {
        resetSession()
        val response = AuthResponse("usr-1", "Test User", "test@example.com", "token-abc")
        UserSession.login(response)

        assertTrue(UserSession.isLoggedIn)
        assertNotNull(UserSession.currentUser)
        assertEquals("usr-1", UserSession.currentUser?.userId)
    }

    @Test
    fun `userName returns user name when logged in`() {
        resetSession()
        UserSession.login(AuthResponse("id", "Jane Doe", "jane@test.com", "tok"))
        assertEquals("Jane Doe", UserSession.userName)
    }

    @Test
    fun `userName returns empty string when not logged in`() {
        resetSession()
        assertEquals("", UserSession.userName)
    }

    @Test
    fun `userEmail returns email when logged in`() {
        resetSession()
        UserSession.login(AuthResponse("id", "Name", "email@test.com", "tok"))
        assertEquals("email@test.com", UserSession.userEmail)
    }

    @Test
    fun `userEmail returns empty string when not logged in`() {
        resetSession()
        assertEquals("", UserSession.userEmail)
    }

    @Test
    fun `logout clears the session`() {
        resetSession()
        UserSession.login(AuthResponse("id", "Name", "email@test.com", "tok"))
        assertTrue(UserSession.isLoggedIn)

        UserSession.logout()
        assertFalse(UserSession.isLoggedIn)
        assertNull(UserSession.currentUser)
    }

    @Test
    fun `login overwrites previous session`() {
        resetSession()
        UserSession.login(AuthResponse("id-1", "First", "first@test.com", "tok1"))
        UserSession.login(AuthResponse("id-2", "Second", "second@test.com", "tok2"))

        assertEquals("id-2", UserSession.currentUser?.userId)
        assertEquals("Second", UserSession.userName)
        assertEquals("second@test.com", UserSession.userEmail)
    }
}
