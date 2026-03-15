package com.example.guardianlink

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import audio.VoicePhraseRecorder
import contacts.DeviceContactsHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import gesture.GestureDetectionEngine
import location.MainActivityHolder
import network.NetworkConnectivityObserver
import screens.initTriggerConfigPlatform
import session.SosAlertSession
import storage.AppStorage
import storage.ContactStorage
import storage.TriggerConfigStorage
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private var lastHandledSosId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppStorage.init(this)
        MainActivityHolder.context = applicationContext
        ContactStorage.init(this)
        DeviceContactsHelper.init(this)
        TriggerConfigStorage.init(this)
        VoicePhraseRecorder.init(this)
        GestureDetectionEngine.init(this)
        NetworkConnectivityObserver.init(this)
        NetworkConnectivityObserver.start()
        PushRegistrationSync.init(this)
        initTriggerConfigPlatform(this)
        MonitoringServiceController.syncWithStoredPreference(this)
        handleIncomingSosIntent(intent)

        setContent {
            App()
        }
    }

    override fun onStart() {
        super.onStart()
        handleIncomingSosIntent(intent)
        logFcmToken()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingSosIntent(intent)
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.action == KeyEvent.ACTION_DOWN) {
            GestureDetectionEngine.notifyVolumeDownPress()
        }
        return super.dispatchKeyEvent(event)
    }

    private fun logFcmToken() {
        val app = ensureFirebaseInitialized()
        if (app == null) {
            Log.w("FCM_TOKEN", "FirebaseApp not initialized; place google-services.json in src/androidMain/assets")
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d("FCM_TOKEN", token)
                PushRegistrationSync.sync(fcmToken = token, reason = "app_start")
            }
            .addOnFailureListener { error ->
                Log.w("FCM_TOKEN", "Failed to fetch FCM token", error)
            }
    }

    private fun ensureFirebaseInitialized(): FirebaseApp? {
        FirebaseApp.getApps(this).firstOrNull()?.let { return it }

        // Try resource-based init first (works when Google Services plugin/resources are present).
        FirebaseApp.initializeApp(this)?.let { return it }

        // Fallback for this KMP project: initialize directly from assets/google-services.json.
        return runCatching {
            val raw = assets.open("google-services.json").bufferedReader().use { it.readText() }
            val root = JSONObject(raw)

            // Common setup mistake: copying Firebase service-account key JSON here.
            if (!root.has("project_info") || !root.has("client")) {
                if (root.optString("type") == "service_account") {
                    Log.w(
                        "FCM_TOKEN",
                        "Wrong file type: service-account key detected. Download app config google-services.json from Firebase Project settings > General > Your apps."
                    )
                }
                return null
            }

            val projectInfo = root.getJSONObject("project_info")
            val clients = root.getJSONArray("client")
            var client: JSONObject? = null
            for (i in 0 until clients.length()) {
                val item = clients.getJSONObject(i)
                val androidInfo = item.optJSONObject("client_info")
                    ?.optJSONObject("android_client_info")
                if (androidInfo?.optString("package_name") == packageName) {
                    client = item
                    break
                }
            }
            if (client == null && clients.length() > 0) {
                client = clients.getJSONObject(0)
            }
            client ?: return null

            val clientInfo = client.getJSONObject("client_info")
            val apiKey = client.getJSONArray("api_key").getJSONObject(0).getString("current_key")

            val options = FirebaseOptions.Builder()
                .setApplicationId(clientInfo.getString("mobilesdk_app_id"))
                .setProjectId(projectInfo.getString("project_id"))
                .setGcmSenderId(projectInfo.getString("project_number"))
                .setApiKey(apiKey)
                .build()

            FirebaseApp.initializeApp(this, options)
        }.onFailure {
            Log.w("FCM_TOKEN", "Firebase asset init failed", it)
        }.getOrNull()
    }

    private fun handleIncomingSosIntent(intent: android.content.Intent?) {
        val src = intent?.getStringExtra("type")
        if (src != "SOS_ALERT") return

        val victimName = intent.getStringExtra("victimName")?.ifBlank { null } ?: return
        val sosId = intent.getStringExtra("sosId")?.ifBlank { null } ?: "sos-${System.currentTimeMillis()}"
        if (sosId == lastHandledSosId) return
        lastHandledSosId = sosId

        val helpText = intent.getStringExtra("helpText")
            ?.ifBlank { null }
            ?: "$victimName might need help"

        val lat = intent.getStringExtra("lat")?.toDoubleOrNull()
        val lng = intent.getStringExtra("lng")?.toDoubleOrNull()

        SosAlertSession.set(
            SosAlertSession.Alert(
                sosId = sosId,
                victimName = victimName,
                helpText = helpText,
                lat = lat,
                lng = lng,
            )
        )

        // Consume extras so onStart does not retrigger the same alert navigation repeatedly.
        intent.removeExtra("type")
        intent.removeExtra("sosId")
        intent.removeExtra("victimName")
        intent.removeExtra("helpText")
        intent.removeExtra("lat")
        intent.removeExtra("lng")
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}