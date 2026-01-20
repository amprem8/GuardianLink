package com.example.guardianlink

import auth.AuthService
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import kotlinx.serialization.json.Json
import models.AuthResponse
import models.SignupRequest

object SignupHandler {

    fun handle(request: APIGatewayV2HTTPEvent, json: Json): APIGatewayV2HTTPResponse {
        val body = request.body ?: return badRequest("Missing body")
        val signup = json.decodeFromString<SignupRequest>(body)

        // Call your actual AuthService
        val resp: AuthResponse = AuthService.signup(signup)

        return ok(json.encodeToString(resp))
    }

    private fun ok(body: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(201)
            .withHeaders(defaultHeaders())
            .withBody(body)
            .build()

    private fun badRequest(msg: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(400)
            .withHeaders(defaultHeaders())
            .withBody("""{"message":"$msg"}""")
            .build()

    private fun defaultHeaders() = mapOf(
        "Content-Type" to "application/json",
        "Access-Control-Allow-Origin" to "*"
    )
}
