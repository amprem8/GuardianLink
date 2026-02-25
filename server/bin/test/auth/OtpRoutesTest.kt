package auth

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.mockk.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OtpRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ── sendOtp tests ──

    @Test
    fun `sendOtp returns 400 for invalid short phone number`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns """{"phone":"12345"}"""

        val response = OtpRoutes.sendOtp(event)
        assertEquals(400, response.statusCode)
        assertTrue(response.body.contains("INVALID_PHONE"))
    }

    @Test
    fun `sendOtp returns 200 for valid phone number`() {
        mockkObject(OtpService)
        try {
            every { OtpService.sendOtp(any()) } just runs

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"phone":"9345678901"}"""

            val response = OtpRoutes.sendOtp(event)
            assertEquals(200, response.statusCode)
            assertTrue(response.body.contains("success"))
            verify { OtpService.sendOtp("+919345678901") }
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `sendOtp normalizes phone with +91 prefix`() {
        mockkObject(OtpService)
        try {
            every { OtpService.sendOtp(any()) } just runs

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"phone":"+919345678901"}"""

            val response = OtpRoutes.sendOtp(event)
            assertEquals(200, response.statusCode)
            verify { OtpService.sendOtp("+919345678901") }
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `sendOtp normalizes phone with 91 prefix`() {
        mockkObject(OtpService)
        try {
            every { OtpService.sendOtp(any()) } just runs

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"phone":"919345678901"}"""

            val response = OtpRoutes.sendOtp(event)
            assertEquals(200, response.statusCode)
            verify { OtpService.sendOtp("+919345678901") }
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `sendOtp returns 500 when OtpService throws`() {
        mockkObject(OtpService)
        try {
            every { OtpService.sendOtp(any()) } throws RuntimeException("AWS error")

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"phone":"9345678901"}"""

            val response = OtpRoutes.sendOtp(event)
            assertEquals(500, response.statusCode)
            assertTrue(response.body.contains("SMS_FAILED"))
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `sendOtp handles null body`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns null

        // Parsing "{}" should result in missing phone field
        val response = OtpRoutes.sendOtp(event)
        // Either 400 for missing phone or 500 for parse error
        assertTrue(response.statusCode in listOf(400, 500))
    }

    // ── verifyOtp tests ──

    @Test
    fun `verifyOtp returns 400 for non-6-digit OTP`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns """{"phone":"9345678901","otp":"123"}"""

        val response = OtpRoutes.verifyOtp(event)
        assertEquals(400, response.statusCode)
        assertTrue(response.body.contains("INVALID_OTP"))
    }

    @Test
    fun `verifyOtp returns 400 for alphabetic OTP`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns """{"phone":"9345678901","otp":"abcdef"}"""

        val response = OtpRoutes.verifyOtp(event)
        assertEquals(400, response.statusCode)
        assertTrue(response.body.contains("INVALID_OTP"))
    }

    @Test
    fun `verifyOtp returns 200 for valid OTP`() {
        mockkObject(OtpService)
        try {
            every { OtpService.verifyOtp(any(), any()) } returns true

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"phone":"9345678901","otp":"123456"}"""

            val response = OtpRoutes.verifyOtp(event)
            assertEquals(200, response.statusCode)
            assertTrue(response.body.contains("success"))
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `verifyOtp returns 401 for wrong OTP`() {
        mockkObject(OtpService)
        try {
            every { OtpService.verifyOtp(any(), any()) } returns false

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"phone":"9345678901","otp":"999999"}"""

            val response = OtpRoutes.verifyOtp(event)
            assertEquals(401, response.statusCode)
            assertTrue(response.body.contains("OTP_INVALID"))
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `verifyOtp returns 500 when OtpService throws`() {
        mockkObject(OtpService)
        try {
            every { OtpService.verifyOtp(any(), any()) } throws RuntimeException("DB error")

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"phone":"9345678901","otp":"123456"}"""

            val response = OtpRoutes.verifyOtp(event)
            assertEquals(500, response.statusCode)
            assertTrue(response.body.contains("VERIFY_FAILED"))
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `verifyOtp handles null body`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns null

        val response = OtpRoutes.verifyOtp(event)
        // Should handle gracefully (400 or 500)
        assertTrue(response.statusCode in listOf(400, 500))
    }

    // ── CORS tests ──

    @Test
    fun `response headers do not contain wildcard CORS`() {
        mockkObject(OtpService)
        try {
            every { OtpService.sendOtp(any()) } just runs

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"phone":"9345678901"}"""

            val response = OtpRoutes.sendOtp(event)
            val corsHeader = response.headers?.get("Access-Control-Allow-Origin")
            if (corsHeader != null) {
                assertTrue(corsHeader != "*", "CORS must not use wildcard")
            }
        } finally {
            unmockkAll()
        }
    }
}
