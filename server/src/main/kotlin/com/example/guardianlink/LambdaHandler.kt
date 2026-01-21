package com.example.guardianlink

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import kotlinx.serialization.json.Json

class LambdaHandler : RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private val json = Json { ignoreUnknownKeys = true }

    override fun handleRequest(
        request: APIGatewayV2HTTPEvent,
        context: Context
    ): APIGatewayV2HTTPResponse {

        val path = request.rawPath ?: "/"
        val method = request.requestContext.http.method ?: "GET"

        context.logger.log("Request: $method $path")

        return try {
                when (path) {
                    "/signup" -> SignupHandler.handle(request, json)
                    "/login"  -> LoginHandler.handle(request, json)
                    else      -> HttpResponses.notFound()
                }
        } catch (e: Exception) {
            context.logger.log("ERROR: ${e.stackTraceToString()}")
            HttpResponses.internalError()
        }
    }

    private fun response(status: Int, body: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(status)
            .withHeaders(
                mapOf(
                    "Content-Type" to "application/json",
                    "Access-Control-Allow-Origin" to "*"
                )
            )
            .withBody(body)
            .build()
}
