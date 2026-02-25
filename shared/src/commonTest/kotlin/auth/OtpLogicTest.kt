package auth

import api.AuthApiContract
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import util.ApiError
import util.ApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class OtpLogicTest {

    // ── Fake AuthApi ──

    private class FakeAuthApi : AuthApiContract {
        var sendOtpResult: Result<Unit> = Result.success(Unit)
        var verifyOtpResult: Result<Unit> = Result.success(Unit)

        var lastSentPhone: String? = null
        var lastVerifiedPhone: String? = null
        var lastVerifiedOtp: String? = null

        override suspend fun sendOtp(phone: String): Result<Unit> {
            lastSentPhone = phone
            return sendOtpResult
        }

        override suspend fun verifyOtp(phone: String, otp: String): Result<Unit> {
            lastVerifiedPhone = phone
            lastVerifiedOtp = otp
            return verifyOtpResult
        }
    }

    private fun createLogic(
        fakeApi: FakeAuthApi = FakeAuthApi(),
        testScope: TestScope = TestScope()
    ): Pair<OtpLogic, FakeAuthApi> {
        val logic = OtpLogic(fakeApi, testScope)
        return logic to fakeApi
    }

    // ── Initial state ──

    @Test
    fun `initial uiState is PhoneEntry`() {
        val (logic, _) = createLogic()
        assertIs<OtpUiState.PhoneEntry>(logic.uiState.value)
    }

    @Test
    fun `initial phone is empty`() {
        val (logic, _) = createLogic()
        assertEquals("", logic.phone.value)
    }

    @Test
    fun `initial otp is empty`() {
        val (logic, _) = createLogic()
        assertEquals("", logic.otp.value)
    }

    // ── onPhoneChanged ──

    @Test
    fun `onPhoneChanged updates phone state`() {
        val (logic, _) = createLogic()
        logic.onPhoneChanged("9345678901")
        assertEquals("9345678901", logic.phone.value)
    }

    @Test
    fun `onPhoneChanged overwrites previous value`() {
        val (logic, _) = createLogic()
        logic.onPhoneChanged("111")
        logic.onPhoneChanged("222")
        assertEquals("222", logic.phone.value)
    }

    // ── onOtpChanged ──

    @Test
    fun `onOtpChanged updates otp state`() {
        val (logic, _) = createLogic()
        logic.onOtpChanged("123456")
        assertEquals("123456", logic.otp.value)
    }

    @Test
    fun `onOtpChanged overwrites previous value`() {
        val (logic, _) = createLogic()
        logic.onOtpChanged("111111")
        logic.onOtpChanged("999999")
        assertEquals("999999", logic.otp.value)
    }

    // ── sendOtp – validation (synchronous) ──

    @Test
    fun `sendOtp with empty phone sets Error state`() {
        val (logic, _) = createLogic()
        logic.sendOtp()
        assertIs<OtpUiState.Error>(logic.uiState.value)
        assertEquals(
            "Enter a valid Indian mobile number",
            (logic.uiState.value as OtpUiState.Error).message
        )
    }

    @Test
    fun `sendOtp with too short phone sets Error state`() {
        val (logic, _) = createLogic()
        logic.onPhoneChanged("12345")
        logic.sendOtp()
        assertIs<OtpUiState.Error>(logic.uiState.value)
    }

    @Test
    fun `sendOtp with phone starting with 0 sets Error state`() {
        val (logic, _) = createLogic()
        logic.onPhoneChanged("0123456789")
        logic.sendOtp()
        assertIs<OtpUiState.Error>(logic.uiState.value)
    }

    @Test
    fun `sendOtp with phone starting with 5 sets Error state`() {
        val (logic, _) = createLogic()
        logic.onPhoneChanged("5123456789")
        logic.sendOtp()
        assertIs<OtpUiState.Error>(logic.uiState.value)
    }

    @Test
    fun `sendOtp with alphabetic input sets Error state`() {
        val (logic, _) = createLogic()
        logic.onPhoneChanged("abcdefghij")
        logic.sendOtp()
        assertIs<OtpUiState.Error>(logic.uiState.value)
    }

    @Test
    fun `sendOtp with 11 digits sets Error state`() {
        val (logic, _) = createLogic()
        logic.onPhoneChanged("93456789012") // 11 digits
        logic.sendOtp()
        assertIs<OtpUiState.Error>(logic.uiState.value)
    }

    // ── sendOtp – valid phone, API success ──

    @Test
    fun `sendOtp with valid phone calls API and transitions to OtpEntry on success`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("9345678901")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        assertIs<OtpUiState.OtpEntry>(logic.uiState.value)
        assertEquals("+919345678901", fakeApi.lastSentPhone)
    }

    @Test
    fun `sendOtp normalizes phone by stripping non-digits`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("93-456-78901")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        assertEquals("+919345678901", fakeApi.lastSentPhone)
    }

    @Test
    fun `sendOtp accepts phone starting with 6`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("6000000000")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        assertIs<OtpUiState.OtpEntry>(logic.uiState.value)
    }

    @Test
    fun `sendOtp accepts phone starting with 7`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("7000000000")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        assertIs<OtpUiState.OtpEntry>(logic.uiState.value)
    }

    @Test
    fun `sendOtp accepts phone starting with 8`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("8000000000")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        assertIs<OtpUiState.OtpEntry>(logic.uiState.value)
    }

    // ── sendOtp – valid phone, API failure ──

    @Test
    fun `sendOtp shows ApiException message on failure`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        fakeApi.sendOtpResult = Result.failure(
            ApiException(ApiError("SMS_FAILED", "SMS service unavailable"))
        )
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("9345678901")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        val state = logic.uiState.value
        assertIs<OtpUiState.Error>(state)
        assertEquals("SMS service unavailable", state.message)
    }

    @Test
    fun `sendOtp shows connectivity message for non-API errors`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        fakeApi.sendOtpResult = Result.failure(RuntimeException("Connection refused"))
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("9345678901")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        val state = logic.uiState.value
        assertIs<OtpUiState.Error>(state)
        assertEquals("Unable to connect. Please check your internet connection.", state.message)
    }

    // ── verifyOtp ──

    @Test
    fun `verifyOtp transitions to Verifying then Success`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("9345678901")
        logic.onOtpChanged("123456")
        logic.verifyOtp()

        // Before coroutine completes, state should be Verifying
        assertIs<OtpUiState.Verifying>(logic.uiState.value)

        testScope.advanceUntilIdle()

        assertIs<OtpUiState.Success>(logic.uiState.value)
        assertEquals("+919345678901", fakeApi.lastVerifiedPhone)
        assertEquals("123456", fakeApi.lastVerifiedOtp)
    }

    @Test
    fun `verifyOtp shows error on ApiException`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        fakeApi.verifyOtpResult = Result.failure(
            ApiException(ApiError("OTP_INVALID", "Invalid or expired OTP"))
        )
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("9345678901")
        logic.onOtpChanged("000000")
        logic.verifyOtp()

        testScope.advanceUntilIdle()

        val state = logic.uiState.value
        assertIs<OtpUiState.Error>(state)
        assertEquals("Invalid or expired OTP", state.message)
    }

    @Test
    fun `verifyOtp shows generic error on non-API failure`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        fakeApi.verifyOtpResult = Result.failure(RuntimeException("timeout"))
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("9345678901")
        logic.onOtpChanged("123456")
        logic.verifyOtp()

        testScope.advanceUntilIdle()

        val state = logic.uiState.value
        assertIs<OtpUiState.Error>(state)
        assertEquals("Something went wrong", state.message)
    }

    // ── dismissError ──

    @Test
    fun `dismissError resets state to PhoneEntry`() {
        val (logic, _) = createLogic()

        // First put it in error state
        logic.sendOtp() // empty phone → Error
        assertIs<OtpUiState.Error>(logic.uiState.value)

        logic.dismissError()
        assertIs<OtpUiState.PhoneEntry>(logic.uiState.value)
    }

    @Test
    fun `dismissError works from any error`() {
        val (logic, _) = createLogic()

        logic.onPhoneChanged("12345")
        logic.sendOtp()
        assertIs<OtpUiState.Error>(logic.uiState.value)

        logic.dismissError()
        assertIs<OtpUiState.PhoneEntry>(logic.uiState.value)
    }

    // ── Phone normalization edge cases ──

    @Test
    fun `sendOtp strips spaces from phone`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("934 567 8901")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        assertEquals("+919345678901", fakeApi.lastSentPhone)
    }

    @Test
    fun `sendOtp strips parentheses and dashes from phone`() = runTest {
        val testScope = TestScope(testScheduler)
        val fakeApi = FakeAuthApi()
        val logic = OtpLogic(fakeApi, testScope)

        logic.onPhoneChanged("(934) 567-8901")
        logic.sendOtp()

        testScope.advanceUntilIdle()

        assertEquals("+919345678901", fakeApi.lastSentPhone)
    }
}
