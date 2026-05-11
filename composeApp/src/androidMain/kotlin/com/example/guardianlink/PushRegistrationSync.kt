package com.example.guardianlink

import android.content.Context
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.PushRegistrationApi
import storage.AppStorage

object PushRegistrationSync {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun syncFromService(context: Context, fcmToken: String, reason: String) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
        scope.launch { doSync(fcmToken, reason) }
    }

    /** Fire-and-forget registration (used at app start / token refresh). */
    fun sync(fcmToken: String, reason: String) {
        if (fcmToken.isBlank()) return
        scope.launch { doSync(fcmToken, reason) }
    }

    /** Suspend until registration completes — used before SOS dispatch. */
    suspend fun syncAndAwait(fcmToken: String, reason: String) {
        if (fcmToken.isBlank()) return
        // Run on IO dispatcher, directly calling the suspend register — NO runBlocking
        withContext(Dispatchers.IO) { doSync(fcmToken, reason) }
    }

    // suspend so it can call PushRegistrationApi.register() directly without runBlocking
    private suspend fun doSync(fcmToken: String, reason: String) {
        if (fcmToken.isBlank()) return

        if (!::appContext.isInitialized) {
            Log.w("FCM_REGISTER", "Skipped ($reason): PushRegistrationSync not initialized")
            return
        }

        AppStorage.init(appContext)
        if (!AppStorage.isLoggedIn()) {
            Log.d("FCM_REGISTER", "Skipped ($reason): user not logged in")
            return
        }

        val phone = AppStorage.getPhoneNumber().trim()
        if (phone.isBlank()) {
            Log.w("FCM_REGISTER", "Skipped ($reason): phone number missing in session")
            return
        }

        val deviceId = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ANDROID_ID,
        ).orEmpty().ifBlank { "android-${System.currentTimeMillis()}" }

        runCatching {
            PushRegistrationApi.register(
                PushRegistrationApi.RegisterPushRequest(
                    phoneNumber = phone,
                    deviceId = deviceId,
                    fcmToken = fcmToken,
                    platform = "ANDROID",
                )
            )
        }.onSuccess { response ->
            Log.d(
                "FCM_REGISTER",
                "Registered ($reason): phone=${response.phoneNumber} endpoint=${response.endpointArn}",
            )
        }.onFailure { error ->
            Log.w("FCM_REGISTER", "Registration failed ($reason)", error)
        }
    }
}


