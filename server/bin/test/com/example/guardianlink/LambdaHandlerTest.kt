package com.example.guardianlink

import auth.OtpRoutes
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Http
import io.mockk.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LambdaHandlerTest {

    private val handler = LambdaHandler()

    private fun createEvent(path: String, method: String = "POST", body: String? = null): APIGatewayV2HTTPEvent {
        val http = mockk<Http>()
        every { http.method } returns method

        val requestContext = mockk<RequestContext>()
        every { requestContext.http } returns http

        val event = mockk<APIGatewayV2HTTPEvent>()
        every { event.rawPath } returns path
        every { event.requestContext } returns requestContext
        every { event.body } returns body

        return event
    }

    private fun createContext(): Context {
        val logger = mockk<LambdaLogger>()
        every { logger.log(any<String>()) } just runs

        val context = mockk<Context>()
        every { context.logger } returns logger

        return context
    }

    @Test
    fun `routes signup to SignupHandler`() {
        mockkObject(SignupHandler)
        try {
            val expectedResponse = HttpResponses.created("""{"userId":"1"}""")
            every { SignupHandler.handle(any(), any()) } returns expectedResponse

            val event = createEvent("/signup", body = """{"email":"a@b.com","password":"12345678","name":"Test"}""")
            val context = createContext()

            val response = handler.handleRequest(event, context)
            assertEquals(201, response.statusCode)
            verify { SignupHandler.handle(any(), any()) }
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `routes login to LoginHandler`() {
        mockkObject(LoginHandler)
        try {
            val expectedResponse = HttpResponses.ok("""{"userId":"1"}""")
            every { LoginHandler.handle(any(), any()) } returns expectedResponse

            val event = createEvent("/login", body = """{"email":"a@b.com","password":"12345678"}""")
            val context = createContext()

            val response = handler.handleRequest(event, context)
            assertEquals(200, response.statusCode)
            verify { LoginHandler.handle(any(), any()) }
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `routes otp send to OtpRoutes`() {
        mockkObject(OtpRoutes)
        try {
            val expectedResponse = mockk<com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse>()
            every { expectedResponse.statusCode } returns 200
            every { OtpRoutes.sendOtp(any()) } returns expectedResponse

            val event = createEvent("/otp/send", body = """{"phone":"9345678901"}""")
            val context = createContext()

            val response = handler.handleRequest(event, context)
            assertEquals(200, response.statusCode)
            verify { OtpRoutes.sendOtp(any()) }
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `routes otp verify to OtpRoutes`() {
        mockkObject(OtpRoutes)
        try {
            val expectedResponse = mockk<com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse>()
            every { expectedResponse.statusCode } returns 200
            every { OtpRoutes.verifyOtp(any()) } returns expectedResponse

            val event = createEvent("/otp/verify", body = """{"phone":"9345678901","otp":"123456"}""")
            val context = createContext()

            val response = handler.handleRequest(event, context)
            assertEquals(200, response.statusCode)
            verify { OtpRoutes.verifyOtp(any()) }
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `returns 404 for unknown route`() {
        val event = createEvent("/unknown")
        val context = createContext()

        val response = handler.handleRequest(event, context)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun `returns 500 when handler throws exception`() {
        mockkObject(SignupHandler)
        try {
            every { SignupHandler.handle(any(), any()) } throws RuntimeException("Unexpected crash")

            val event = createEvent("/signup", body = """{}""")
            val context = createContext()

            val response = handler.handleRequest(event, context)
            assertEquals(500, response.statusCode)
        } finally {
            unmockkAll()
        }
    }

    @Test
    fun `handles null rawPath gracefully`() {
        val event = createEvent("/", method = "GET")
        every { event.rawPath } returns null
        val context = createContext()

        val response = handler.handleRequest(event, context)
        // null path defaults to "/" which is not a known route
        assertEquals(404, response.statusCode)
    }
}
