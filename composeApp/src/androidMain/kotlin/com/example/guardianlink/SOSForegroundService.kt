package com.example.guardianlink

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import gesture.GestureDetectionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import location.MainActivityHolder
import location.getCurrentLocationOrNull
import network.SosPushApi
import push.refreshPushRegistrationBeforeSos
import storage.AppStorage
import storage.ContactStorage
import storage.TriggerConfigStorage
import util.nowTimestampText

/**
 * Foreground service that keeps the SOS gesture + voice trigger listener alive
 * when the app is in the background.
 *
 * Android 14+ (API 34) requires:
 *  - foregroundServiceType declared in AndroidManifest.xml
 *  - Corresponding runtime permissions granted before starting the service
 *
 * This is currently a structural stub. The actual gesture/voice detection logic
 * will be wired in Phase 2.
 */
class SOSForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var sosDispatchInFlight = false

    companion object {
        const val CHANNEL_ID = "sos_listener_channel"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "SOSForegroundService"
    }

    override fun onCreate() {
        super.onCreate()
        AppStorage.init(this)
        ContactStorage.init(this)
        TriggerConfigStorage.init(this)
        GestureDetectionEngine.init(this)
        MainActivityHolder.context = applicationContext
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()

        // Keep FGS type to location only for background eligibility on Android 14+.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (se: SecurityException) {
            Log.w(TAG, "Unable to enter foreground mode for SOS service", se)
            stopSelf()
            return START_NOT_STICKY
        }

        if (!AppStorage.isContinuousMonitoring()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val selectedGesture = TriggerConfigStorage.loadConfig().gestureType
        GestureDetectionEngine.init(this)
        val started = GestureDetectionEngine.start(selectedGesture) {
            if (isAppInForeground()) return@start false
            Log.i(TAG, "Gesture detected in continuous monitoring mode: $selectedGesture")
            AppStorage.setLastGestureTriggeredText("$selectedGesture at ${nowTimestampText()}")
            dispatchSosFromBackground()
            true
        }

        if (!started) {
            Log.w(TAG, "Gesture engine unavailable for selected gesture: $selectedGesture")
        }

        return START_STICKY // Restart if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        GestureDetectionEngine.stop()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun dispatchSosFromBackground() {
        if (sosDispatchInFlight) {
            Log.d(TAG, "Skipping SOS dispatch: previous dispatch still in flight")
            return
        }

        sosDispatchInFlight = true
        serviceScope.launch {
            try {
                val contacts = ContactStorage.loadContacts()
                if (contacts.isEmpty()) {
                    Log.w(TAG, "Skipping SOS dispatch: no emergency contacts configured")
                    return@launch
                }

                refreshPushRegistrationBeforeSos()

                val victimName = AppStorage.getUserName().ifBlank { "User" }
                val victimUserId = AppStorage.getUserEmail()
                    .ifBlank { AppStorage.getPhoneNumber().ifBlank { "user" } }

                val latestLocation = getCurrentLocationOrNull()
                val location = SosPushApi.SosLocationContext(
                    permissionGranted = latestLocation != null,
                    gpsEnabled = latestLocation != null,
                    lat = latestLocation?.latitude,
                    lng = latestLocation?.longitude,
                )

                val response = SosPushApi.trigger(
                    SosPushApi.SosPushRequest(
                        victimUserId = victimUserId,
                        victimName = victimName,
                        location = location,
                        contacts = contacts.map { c ->
                            SosPushApi.SosContactTarget(
                                contactName = c.name,
                                phoneNumber = c.phone,
                                endpointArn = "",
                                includeGPS = c.includeGPS,
                            )
                        },
                    )
                )

                if (response.sentCount > 0) {
                    AppStorage.setLastSosSentText("Sent at ${nowTimestampText()}")
                }
                Log.i(TAG, "Background SOS dispatch complete: sent=${response.sentCount}, failed=${response.failedCount}")
            } catch (t: Throwable) {
                Log.w(TAG, "Background SOS dispatch failed", t)
            } finally {
                sosDispatchInFlight = false
            }
        }
    }

    private fun isAppInForeground(): Boolean {
        val proc = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(proc)
        return proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SOS Listener",
                NotificationManager.IMPORTANCE_LOW // Low = no sound, but persistent
            ).apply {
                description = "Keeps ResQ ready to detect emergency gestures and voice commands"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ResQ is active")
            .setContentText("Listening for emergency trigger")
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // TODO: Replace with app icon
            .setOngoing(true)        // Cannot be swiped away
            .setAutoCancel(false)    // Cannot be dismissed by tapping
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}
