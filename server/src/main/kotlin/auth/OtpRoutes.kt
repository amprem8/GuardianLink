package auth

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object OtpRoutes {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    data class SendOtpRequest(val phone: String)

    @Serializable
    data class VerifyOtpRequest(val phone: String, val otp: String)

    private fun normalizePhone(phone: String): String {
        val digits = phone.replace("\\D".toRegex(), "")
        return "+91$digits"
    }

    fun sendOtp(event: APIGatewayV2HTTPEvent): APIGatewayV2HTTPResponse {
        val req = json.decodeFromString<SendOtpRequest>(event.body ?: "{}")

        OtpService.sendOtp(normalizePhone(req.phone))

        return ok("""{"success":true}""")
    }

    fun verifyOtp(event: APIGatewayV2HTTPEvent): APIGatewayV2HTTPResponse {
        val req = json.decodeFromString<VerifyOtpRequest>(event.body ?: "{}")

        if (!req.otp.matches(Regex("^\\d{6}$"))) {
            return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(400)
                .withHeaders(
                    mapOf(
                        "Content-Type" to "application/json",
                        "Access-Control-Allow-Origin" to "*"
                    )
                )
                .withBody("""{"error":"OTP must be 6 digits"}""")
                .build()
        }

        val valid = OtpService.verifyOtp(normalizePhone(req.phone), req.otp)

        return ok("""$valid""")   // <- IMPORTANT, client expects Boolean
    }

    private fun ok(body: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(200)
            .withHeaders(
                mapOf(
                    "Content-Type" to "application/json",
                    "Access-Control-Allow-Origin" to "*"
                )
            )
            .withBody(body)
            .build()
}
