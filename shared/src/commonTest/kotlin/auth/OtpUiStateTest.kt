package auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class OtpUiStateTest {

    @Test
    fun `PhoneEntry is a singleton`() {
        val a = OtpUiState.PhoneEntry
        val b = OtpUiState.PhoneEntry
        assertEquals(a, b)
    }

    @Test
    fun `OtpEntry is a singleton`() {
        val a = OtpUiState.OtpEntry
        val b = OtpUiState.OtpEntry
        assertEquals(a, b)
    }

    @Test
    fun `Loading is a singleton`() {
        val a = OtpUiState.Loading
        val b = OtpUiState.Loading
        assertEquals(a, b)
    }

    @Test
    fun `Verifying is a singleton`() {
        val a = OtpUiState.Verifying
        val b = OtpUiState.Verifying
        assertEquals(a, b)
    }

    @Test
    fun `Success is a singleton`() {
        val a = OtpUiState.Success
        val b = OtpUiState.Success
        assertEquals(a, b)
    }

    @Test
    fun `Error stores message`() {
        val error = OtpUiState.Error("Something went wrong")
        assertEquals("Something went wrong", error.message)
    }

    @Test
    fun `Error data class equality`() {
        val e1 = OtpUiState.Error("msg")
        val e2 = OtpUiState.Error("msg")
        assertEquals(e1, e2)
    }

    @Test
    fun `Error with different messages are not equal`() {
        val e1 = OtpUiState.Error("first")
        val e2 = OtpUiState.Error("second")
        assertNotEquals(e1, e2)
    }

    @Test
    fun `all states are subtypes of OtpUiState`() {
        val states: List<OtpUiState> = listOf(
            OtpUiState.PhoneEntry,
            OtpUiState.OtpEntry,
            OtpUiState.Loading,
            OtpUiState.Verifying,
            OtpUiState.Success,
            OtpUiState.Error("test")
        )
        assertEquals(6, states.size)
        assertTrue(states.all { it is OtpUiState })
    }

    @Test
    fun `can pattern match on sealed class`() {
        val state: OtpUiState = OtpUiState.Error("network error")
        when (state) {
            is OtpUiState.Error -> assertEquals("network error", state.message)
            else -> throw AssertionError("Expected Error state")
        }
    }

    @Test
    fun `PhoneEntry is correct type`() {
        assertIs<OtpUiState.PhoneEntry>(OtpUiState.PhoneEntry)
    }

    @Test
    fun `Loading is correct type`() {
        assertIs<OtpUiState.Loading>(OtpUiState.Loading)
    }
}
