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
        return try {
            val req = json.decodeFromString<SendOtpRequest>(event.body ?: "{}")
            val phone = normalizePhone(req.phone)

            if (phone.length < 13) { // +91 + 10 digits
                return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withHeaders(mapOf("Content-Type" to "application/json", "Access-Control-Allow-Origin" to "*"))
                    .withBody("""{"code":"INVALID_PHONE","message":"Please enter a valid 10-digit phone number"}""")
                    .build()
            }

            OtpService.sendOtp(phone)
            ok("""{"success":true}""")
        } catch (e: Exception) {
            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(500)
                .withHeaders(mapOf("Content-Type" to "application/json", "Access-Control-Allow-Origin" to "*"))
                .withBody("""{"code":"SMS_FAILED","message":"Failed to send OTP. Please try again."}""")
                .build()
        }
    }

    fun verifyOtp(event: APIGatewayV2HTTPEvent): APIGatewayV2HTTPResponse {
        return try {
            val req = json.decodeFromString<VerifyOtpRequest>(event.body ?: "{}")

            if (!req.otp.matches(Regex("^\\d{6}$"))) {
                return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(400)
                    .withHeaders(mapOf("Content-Type" to "application/json", "Access-Control-Allow-Origin" to "*"))
                    .withBody("""{"code":"INVALID_OTP","message":"OTP must be 6 digits"}""")
                    .build()
            }

            val valid = OtpService.verifyOtp(normalizePhone(req.phone), req.otp)

            if (valid) {
                ok("""{"success":true}""")
            } else {
                APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(401)
                    .withHeaders(mapOf("Content-Type" to "application/json", "Access-Control-Allow-Origin" to "*"))
                    .withBody("""{"code":"OTP_INVALID","message":"Invalid or expired OTP"}""")
                    .build()
            }
        } catch (e: Exception) {
            APIGatewayV2HTTPResponse.builder()
                .withStatusCode(500)
                .withHeaders(mapOf("Content-Type" to "application/json", "Access-Control-Allow-Origin" to "*"))
                .withBody("""{"code":"VERIFY_FAILED","message":"Verification failed. Please try again."}""")
                .build()
        }
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
