package auth

object OtpService {

    private const val DEV_OTP = "123456"

    fun sendOtp(phone: String) {
        // In development we do nothing.
        // Pretend SMS was sent successfully.
        println("DEV MODE: OTP for $phone is $DEV_OTP")
    }

    fun verifyOtp(phone: String, otp: String): Boolean {
        return otp == DEV_OTP
    }
}
