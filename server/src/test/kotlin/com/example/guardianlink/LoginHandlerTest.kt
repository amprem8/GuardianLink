package com.example.guardianlink

import auth.AuthService
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.mockk.*
import kotlinx.serialization.json.Json
import models.AuthResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginHandlerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `handle returns 400 for null body`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns null

        val response = LoginHandler.handle(event, json)
        assertEquals(400, response.statusCode)
    }

    @Test
    fun `handle returns 200 for valid login`() {
        mockkObject(AuthService)
        try {
            val authResponse = AuthResponse("usr-1", "Test", "test@example.com", "token-abc")
            every { AuthService.login(any()) } returns authResponse

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"email":"test@example.com","password":"password123"}"""

            val response = LoginHandler.handle(event, json)
            assertEquals(200, response.statusCode)
            assertTrue(response.body.contains("token-abc"))
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `handle returns 401 for invalid credentials`() {
        mockkObject(AuthService)
        try {
            every { AuthService.login(any()) } returns null

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"email":"test@example.com","password":"wrong"}"""

            val response = LoginHandler.handle(event, json)
            assertEquals(401, response.statusCode)
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `handle returns 500 for unexpected error`() {
        mockkObject(AuthService)
        try {
            every { AuthService.login(any()) } throws RuntimeException("Unexpected")

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"email":"test@example.com","password":"password123"}"""

            val response = LoginHandler.handle(event, json)
            assertEquals(500, response.statusCode)
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `handle returns 500 for invalid JSON body`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns "{broken json"

        val response = LoginHandler.handle(event, json)
        assertEquals(500, response.statusCode)
    }
}
