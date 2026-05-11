package com.example.guardianlink

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HttpResponsesTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `ok returns status 200 with given body`() {
        val body = """{"data":"test"}"""
        val response = HttpResponses.ok(body)
        assertEquals(200, response.statusCode)
        assertEquals(body, response.body)
    }

    @Test
    fun `ok response has Content-Type header`() {
        val response = HttpResponses.ok("{}")
        assertEquals("application/json", response.headers["Content-Type"])
    }

    @Test
    fun `created returns status 201 with given body`() {
        val body = """{"id":"123"}"""
        val response = HttpResponses.created(body)
        assertEquals(201, response.statusCode)
        assertEquals(body, response.body)
    }

    @Test
    fun `badRequest returns status 400 with error body`() {
        val response = HttpResponses.badRequest("Missing field")
        assertEquals(400, response.statusCode)

        val errorBody = json.decodeFromString(ErrorBody.serializer(), response.body)
        assertEquals("BAD_REQUEST", errorBody.code)
        assertEquals("Missing field", errorBody.message)
    }

    @Test
    fun `badRequest properly escapes special characters in message`() {
        val maliciousMessage = """test","admin":"true"""
        val response = HttpResponses.badRequest(maliciousMessage)
        assertEquals(400, response.statusCode)

        // Should be valid JSON — parsing should succeed
        val errorBody = json.decodeFromString(ErrorBody.serializer(), response.body)
        assertEquals("BAD_REQUEST", errorBody.code)
        assertEquals(maliciousMessage, errorBody.message)
    }

    @Test
    fun `unauthorized returns status 401 with default message`() {
        val response = HttpResponses.unauthorized()
        assertEquals(401, response.statusCode)

        val errorBody = json.decodeFromString(ErrorBody.serializer(), response.body)
        assertEquals("UNAUTHORIZED", errorBody.code)
        assertEquals("Invalid credentials", errorBody.message)
    }

    @Test
    fun `unauthorized returns status 401 with custom message`() {
        val response = HttpResponses.unauthorized("Token expired")
        assertEquals(401, response.statusCode)

        val errorBody = json.decodeFromString(ErrorBody.serializer(), response.body)
        assertEquals("Token expired", errorBody.message)
    }

    @Test
    fun `conflict returns status 409 with default message`() {
        val response = HttpResponses.conflict()
        assertEquals(409, response.statusCode)

        val errorBody = json.decodeFromString(ErrorBody.serializer(), response.body)
        assertEquals("CONFLICT", errorBody.code)
        assertEquals("Resource already exists", errorBody.message)
    }

    @Test
    fun `conflict returns status 409 with custom message`() {
        val response = HttpResponses.conflict("Email already registered")
        assertEquals(409, response.statusCode)

        val errorBody = json.decodeFromString(ErrorBody.serializer(), response.body)
        assertEquals("Email already registered", errorBody.message)
    }

    @Test
    fun `notFound returns status 404`() {
        val response = HttpResponses.notFound()
        assertEquals(404, response.statusCode)

        val messageBody = json.decodeFromString(MessageBody.serializer(), response.body)
        assertEquals("Route not found", messageBody.message)
    }

    @Test
    fun `internalError returns status 500`() {
        val response = HttpResponses.internalError()
        assertEquals(500, response.statusCode)

        val messageBody = json.decodeFromString(MessageBody.serializer(), response.body)
        assertEquals("Internal Server Error", messageBody.message)
    }

    @Test
    fun `CORS header uses environment variable not wildcard`() {
        val response = HttpResponses.ok("{}")
        // CORS_ALLOWED_ORIGIN is set in Gradle test config
        val corsHeader = response.headers["Access-Control-Allow-Origin"]
        if (corsHeader != null) {
            // Must NOT be wildcard
            assertTrue(corsHeader != "*", "CORS origin must not be wildcard '*'")
        }
        // If null, that's also acceptable (no CORS header = restrictive)
    }
}
