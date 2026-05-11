import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class OtpRequestTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `SendOtpRequest serialization round-trip`() {
        val original = SendOtpRequest("+919345678901")
        val jsonStr = json.encodeToString(SendOtpRequest.serializer(), original)
        val restored = json.decodeFromString(SendOtpRequest.serializer(), jsonStr)
        assertEquals(original, restored)
    }

    @Test
    fun `SendOtpRequest deserialization`() {
        val jsonStr = """{"phone":"+919876543210"}"""
        val parsed = json.decodeFromString(SendOtpRequest.serializer(), jsonStr)
        assertEquals("+919876543210", parsed.phone)
    }

    @Test
    fun `VerifyOtpRequest serialization round-trip`() {
        val original = VerifyOtpRequest("+919345678901", "123456")
        val jsonStr = json.encodeToString(VerifyOtpRequest.serializer(), original)
        val restored = json.decodeFromString(VerifyOtpRequest.serializer(), jsonStr)
        assertEquals(original, restored)
    }

    @Test
    fun `VerifyOtpRequest deserialization`() {
        val jsonStr = """{"phone":"+919876543210","otp":"654321"}"""
        val parsed = json.decodeFromString(VerifyOtpRequest.serializer(), jsonStr)
        assertEquals("+919876543210", parsed.phone)
        assertEquals("654321", parsed.otp)
    }

    @Test
    fun `VerifyOtpRequest fields accessible`() {
        val req = VerifyOtpRequest("phone123", "999999")
        assertEquals("phone123", req.phone)
        assertEquals("999999", req.otp)
    }
}
