package com.example.guardianlink

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ErrorBody(val code: String, val message: String)

@Serializable
data class MessageBody(val message: String)

object HttpResponses {

    private val json = Json { encodeDefaults = true }

    private val defaultHeaders: Map<String, String> = buildMap {
        put("Content-Type", "application/json")
        val corsOrigin = System.getenv("CORS_ALLOWED_ORIGIN")
        if (!corsOrigin.isNullOrEmpty()) {
            put("Access-Control-Allow-Origin", corsOrigin)
        }
    }

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
            .withBody(json.encodeToString(ErrorBody.serializer(), ErrorBody("BAD_REQUEST", message)))
            .build()

    fun unauthorized(message: String = "Invalid credentials") =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(401)
            .withHeaders(defaultHeaders)
            .withBody(json.encodeToString(ErrorBody.serializer(), ErrorBody("UNAUTHORIZED", message)))
            .build()

    fun conflict(message: String = "Resource already exists") =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(409)
            .withHeaders(defaultHeaders)
            .withBody(json.encodeToString(ErrorBody.serializer(), ErrorBody("CONFLICT", message)))
            .build()

    fun notFound() =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(404)
            .withHeaders(defaultHeaders)
            .withBody(json.encodeToString(MessageBody.serializer(), MessageBody("Route not found")))
            .build()

    fun internalError() =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(500)
            .withHeaders(defaultHeaders)
            .withBody(json.encodeToString(MessageBody.serializer(), MessageBody("Internal Server Error")))
            .build()
}
