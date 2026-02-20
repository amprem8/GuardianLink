package com.example.guardianlink

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse

object HttpResponses {

    private val defaultHeaders = mapOf(
        "Content-Type" to "application/json",
        "Access-Control-Allow-Origin" to "*"
    )

    fun ok(body: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(200)
            .withHeaders(defaultHeaders)
            .withBody(body)
            .build()

    fun created(body: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(201)
            .withHeaders(defaultHeaders)
            .withBody(body)
            .build()

    fun badRequest(message: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(400)
            .withHeaders(defaultHeaders)
            .withBody("""{"code":"BAD_REQUEST","message":"$message"}""")
            .build()

    fun unauthorized(message: String = "Invalid credentials") =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(401)
            .withHeaders(defaultHeaders)
            .withBody("""{"code":"UNAUTHORIZED","message":"$message"}""")
            .build()

    fun conflict(message: String = "Resource already exists") =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(409)
            .withHeaders(defaultHeaders)
            .withBody("""{"code":"CONFLICT","message":"$message"}""")
            .build()

    fun notFound() =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(404)
            .withHeaders(defaultHeaders)
            .withBody("""{"message":"Route not found"}""")
            .build()

    fun internalError() =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(500)
            .withHeaders(defaultHeaders)
            .withBody("""{"message":"Internal Server Error"}""")
            .build()
}
