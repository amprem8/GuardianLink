package com.example.guardianlink

import model.EmergencyContact
import screenmodel.EmergencyContactsScreenModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [EmergencyContactsScreenModel] validation & formatting logic.
 *
 * These tests cover the *companion* utility functions (phone validation, formatting)
 * which are pure and don't require platform-specific storage.
 */
class EmergencyContactsValidationTest {

    // ── isValidIndianPhone ────────────────────────────────────

    @Test
    fun validTenDigitPhone_startsWithSix() {
        assertTrue(EmergencyContactsScreenModel.isValidIndianPhone("6123456789"))
    }

    @Test
    fun validTenDigitPhone_startsWithSeven() {
        assertTrue(EmergencyContactsScreenModel.isValidIndianPhone("7123456789"))
    }

    @Test
    fun validTenDigitPhone_startsWithEight() {
        assertTrue(EmergencyContactsScreenModel.isValidIndianPhone("8123456789"))
    }

    @Test
    fun validTenDigitPhone_startsWithNine() {
        assertTrue(EmergencyContactsScreenModel.isValidIndianPhone("9876543210"))
    }

    @Test
    fun validPhoneWithCountryCode() {
        assertTrue(EmergencyContactsScreenModel.isValidIndianPhone("919876543210"))
    }

    @Test
    fun validPhoneWithPlusAndSpaces() {
        assertTrue(EmergencyContactsScreenModel.isValidIndianPhone("+91 9876543210"))
    }

    @Test
    fun invalidPhone_startsWithFive() {
        assertFalse(EmergencyContactsScreenModel.isValidIndianPhone("5123456789"))
    }

    @Test
    fun invalidPhone_startsWithZero() {
        assertFalse(EmergencyContactsScreenModel.isValidIndianPhone("0123456789"))
    }

    @Test
    fun invalidPhone_tooShort() {
        assertFalse(EmergencyContactsScreenModel.isValidIndianPhone("98765"))
    }

    @Test
    fun invalidPhone_tooLong() {
        assertFalse(EmergencyContactsScreenModel.isValidIndianPhone("98765432101"))
    }

    @Test
    fun invalidPhone_empty() {
        assertFalse(EmergencyContactsScreenModel.isValidIndianPhone(""))
    }

    @Test
    fun invalidPhone_nonDigitOnly() {
        assertFalse(EmergencyContactsScreenModel.isValidIndianPhone("abcdefghij"))
    }

    @Test
    fun invalidPhone_countryCodeWithInvalidStart() {
        assertFalse(EmergencyContactsScreenModel.isValidIndianPhone("915123456789"))
    }

    // ── formatIndianPhone ────────────────────────────────────

    @Test
    fun formatTenDigitNumber() {
        assertEquals("+91 9876543210", EmergencyContactsScreenModel.formatIndianPhone("9876543210"))
    }

    @Test
    fun formatTwelveDigitNumber() {
        assertEquals("+91 9876543210", EmergencyContactsScreenModel.formatIndianPhone("919876543210"))
    }

    @Test
    fun formatAlreadyFormatted() {
        val input = "+91 9876543210"
        // Non-digit removal yields "919876543210" → reformats correctly
        assertEquals("+91 9876543210", EmergencyContactsScreenModel.formatIndianPhone(input))
    }

    @Test
    fun formatShortNumber_returnsAsIs() {
        assertEquals("12345", EmergencyContactsScreenModel.formatIndianPhone("12345"))
    }

    // ── EmergencyContact model ──────────────────────────────

    @Test
    fun emergencyContact_defaultValues() {
        val contact = EmergencyContact(
            id = "abc",
            name = "Test",
            phone = "+91 9876543210",
        )
        assertTrue(contact.includeGPS)
        assertTrue(contact.includeAudio)
    }

    @Test
    fun emergencyContact_customValues() {
        val contact = EmergencyContact(
            id = "abc",
            name = "Test",
            phone = "+91 9876543210",
            includeGPS = false,
            includeAudio = false,
        )
        assertFalse(contact.includeGPS)
        assertFalse(contact.includeAudio)
    }

    @Test
    fun emergencyContact_copy_togglesGPS() {
        val original = EmergencyContact(id = "1", name = "A", phone = "+91 9000000000")
        val toggled = original.copy(includeGPS = !original.includeGPS)
        assertFalse(toggled.includeGPS)
        assertTrue(toggled.includeAudio)
    }

    @Test
    fun emergencyContact_copy_togglesAudio() {
        val original = EmergencyContact(id = "1", name = "A", phone = "+91 9000000000")
        val toggled = original.copy(includeAudio = !original.includeAudio)
        assertTrue(toggled.includeGPS)
        assertFalse(toggled.includeAudio)
    }

    // ── Edge cases ──────────────────────────────────────────

    @Test
    fun validPhone_withDashes() {
        // "987-654-3210" → cleaned = "9876543210" → valid
        assertTrue(EmergencyContactsScreenModel.isValidIndianPhone("987-654-3210"))
    }

    @Test
    fun validPhone_withParensAndSpaces() {
        assertTrue(EmergencyContactsScreenModel.isValidIndianPhone("(+91) 98765 43210"))
    }

    @Test
    fun formatPhone_withDashes() {
        assertEquals("+91 9876543210", EmergencyContactsScreenModel.formatIndianPhone("987-654-3210"))
    }
}
