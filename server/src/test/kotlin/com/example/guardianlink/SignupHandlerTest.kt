package com.example.guardianlink

import auth.AuthService
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.mockk.*
import kotlinx.serialization.json.Json
import models.AuthResponse
import models.SignupRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignupHandlerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `handle returns 400 for null body`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns null

        val response = SignupHandler.handle(event, json)
        assertEquals(400, response.statusCode)
    }

    @Test
    fun `handle returns 201 for valid signup`() {
        mockkObject(AuthService)
        try {
            val authResponse = AuthResponse("usr-1", "Test", "test@example.com", "token-123")
            every { AuthService.signup(any()) } returns authResponse

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"email":"test@example.com","password":"password123","name":"Test"}"""

            val response = SignupHandler.handle(event, json)
            assertEquals(201, response.statusCode)
            assertTrue(response.body.contains("usr-1"))
            assertTrue(response.body.contains("token-123"))
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `handle returns 400 for validation error`() {
        mockkObject(AuthService)
        try {
            every { AuthService.signup(any()) } throws IllegalArgumentException("Email is required")

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"email":"","password":"password123","name":"Test"}"""

            val response = SignupHandler.handle(event, json)
            assertEquals(400, response.statusCode)
            assertTrue(response.body.contains("Email is required"))
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `handle returns 409 for duplicate email`() {
        mockkObject(AuthService)
        try {
            every { AuthService.signup(any()) } throws IllegalStateException("An account with this email already exists")

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"email":"dup@example.com","password":"password123","name":"Test"}"""

            val response = SignupHandler.handle(event, json)
            assertEquals(409, response.statusCode)
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `handle returns 500 for unexpected error`() {
        mockkObject(AuthService)
        try {
            every { AuthService.signup(any()) } throws RuntimeException("DB connection failed")

            val event = mockk<APIGatewayV2HTTPEvent>()
            every { event.body } returns """{"email":"test@example.com","password":"password123","name":"Test"}"""

            val response = SignupHandler.handle(event, json)
            assertEquals(500, response.statusCode)
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `handle returns 400 for invalid JSON body`() {
        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.body } returns "not valid json"

        val response = SignupHandler.handle(event, json)
        assertEquals(400, response.statusCode)
    }
}
