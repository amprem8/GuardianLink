package com.example.guardianlink

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LambdaHandler :
    RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    override fun handleRequest(
        request: APIGatewayV2HTTPEvent,
        context: Context
    ): APIGatewayV2HTTPResponse {

        // Start Ktor once per Lambda container
        KtorServer.start()

        return try {
            val path = request.rawPath ?: "/"
            val queryString =
                if (!request.rawQueryString.isNullOrBlank())
                    "?${request.rawQueryString}"
                else ""

            val url = URL("http://127.0.0.1:8080$path$queryString")
            val connection = url.openConnection() as HttpURLConnection

            val method = request.requestContext.http.method
            connection.requestMethod = method
            connection.doInput = true
            connection.doOutput = request.body != null

            request.headers?.forEach { (k, v) ->
                connection.setRequestProperty(k, v)
            }

            request.body?.let { body ->
                connection.outputStream.use { os ->
                    OutputStreamWriter(os, Charsets.UTF_8).use { it.write(body) }
                }
            }

            val responseBody =
                connection.inputStream.bufferedReader().use { it.readText() }

            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(connection.responseCode)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody(responseBody)
                .build()

        } catch (e: Exception) {
            context.logger.log("Lambda error: ${e.message}")
            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(500)
                .withBody("""{"message":"Internal Server Error"}""")
                .build()
        }
    }
}
