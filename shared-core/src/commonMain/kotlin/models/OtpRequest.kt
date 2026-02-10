import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(val phone: String)

@Serializable
data class VerifyOtpRequest(val phone: String, val otp: String)