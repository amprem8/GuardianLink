package api

/**
 * Contract for authentication-related API calls.
 * Enables testing of dependent classes (e.g., OtpLogic) without real HTTP clients.
 */
interface AuthApiContract {
    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, otp: String): Result<Unit>
}
