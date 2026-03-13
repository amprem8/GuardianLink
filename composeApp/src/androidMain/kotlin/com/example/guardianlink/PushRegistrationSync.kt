package com.example.guardianlink

import android.content.Context
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import network.PushRegistrationApi
import storage.AppStorage

object PushRegistrationSync {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun sync(fcmToken: String, reason: String) {
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

        scope.launch {
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
}


