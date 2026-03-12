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
import storage.AppStorage
import storage.TriggerConfigStorage

/**
 * Foreground service that keeps the SOS gesture + voice trigger listener alive
 * when the app is in the background.
 *
 * Android 14+ (API 34) requires:
 *  - foregroundServiceType declared in AndroidManifest.xml (location|microphone)
 *  - Corresponding runtime permissions granted before starting the service
 *
 * This is currently a structural stub. The actual gesture/voice detection logic
 * will be wired in Phase 2.
 */
class SOSForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "sos_listener_channel"
        const val NOTIFICATION_ID = 1001
        private const val TAG = "SOSForegroundService"
    }

    override fun onCreate() {
        super.onCreate()
        AppStorage.init(this)
        TriggerConfigStorage.init(this)
        GestureDetectionEngine.init(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()

        // Android 14+ requires foregroundServiceType at start time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION or
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
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
            // TODO: Wire into full SOS pipeline trigger.
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
        super.onDestroy()
        // TODO Phase 2: Stop gesture detection + voice listener here
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
