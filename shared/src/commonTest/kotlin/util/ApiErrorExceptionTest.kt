package util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApiErrorExceptionTest {

    // ── ApiError ──

    @Test
    fun `ApiError stores code and message`() {
        val error = ApiError("NOT_FOUND", "Resource not found")
        assertEquals("NOT_FOUND", error.code)
        assertEquals("Resource not found", error.message)
    }

    @Test
    fun `ApiError data class equality`() {
        val error1 = ApiError("E1", "msg")
        val error2 = ApiError("E1", "msg")
        assertEquals(error1, error2)
    }

    @Test
    fun `ApiError copy works`() {
        val error = ApiError("ORIG", "original")
        val copy = error.copy(message = "modified")
        assertEquals("ORIG", copy.code)
        assertEquals("modified", copy.message)
    }

    @Test
    fun `ApiError serialization round-trip`() {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val original = ApiError("SERVER_ERROR", "Internal error occurred")
        val jsonStr = json.encodeToString(ApiError.serializer(), original)
        val restored = json.decodeFromString(ApiError.serializer(), jsonStr)
        assertEquals(original, restored)
    }

    @Test
    fun `ApiError deserialization from JSON`() {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val jsonStr = """{"code":"BAD_REQUEST","message":"Invalid input"}"""
        val error = json.decodeFromString(ApiError.serializer(), jsonStr)
        assertEquals("BAD_REQUEST", error.code)
        assertEquals("Invalid input", error.message)
    }

    // ── ApiException ──

    @Test
    fun `ApiException wraps ApiError`() {
        val error = ApiError("AUTH_FAILED", "Authentication failed")
        val exception = ApiException(error)
        assertEquals(error, exception.apiError)
    }

    @Test
    fun `ApiException message comes from ApiError`() {
        val error = ApiError("ERR", "Something broke")
        val exception = ApiException(error)
        assertEquals("Something broke", exception.message)
    }

    @Test
    fun `ApiException is a Throwable`() {
        val error = ApiError("ERR", "Error")
        val exception = ApiException(error)
        assertTrue(exception is Throwable)
    }

    @Test
    fun `ApiException preserves error code`() {
        val error = ApiError("RATE_LIMIT", "Too many requests")
        val exception = ApiException(error)
        assertEquals("RATE_LIMIT", exception.apiError.code)
        assertEquals("Too many requests", exception.apiError.message)
    }
}
