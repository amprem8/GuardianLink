package com.example.guardianlink

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import session.SosAlertSession

class ResqFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "resq_sos_alerts"
        private const val CHANNEL_NAME = "SOS Alerts"
        private const val DEDUPE_WINDOW_MS = 60_000L
        private val recentlyHandled = LinkedHashMap<String, Long>()

        @Synchronized
        private fun shouldHandleOnce(key: String): Boolean {
            if (key.isBlank()) return true
            val now = SystemClock.elapsedRealtime()
            val iterator = recentlyHandled.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now - entry.value > DEDUPE_WINDOW_MS) iterator.remove()
            }
            val seenAt = recentlyHandled[key]
            if (seenAt != null && now - seenAt <= DEDUPE_WINDOW_MS) return false
            recentlyHandled[key] = now
            return true
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Logs every time Firebase rotates/regenerates token.
        Log.d("FCM_TOKEN", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM_PUSH", "Message received. data=${message.data}")

        val payload = message.data

        val victimName = payload["victimName"].orEmpty()
        val sosId = payload["sosId"].orEmpty()
        val helpText = payload["helpText"]?.ifBlank { null } ?: "$victimName might need help"
        val lat = payload["lat"]?.toDoubleOrNull()
        val lng = payload["lng"]?.toDoubleOrNull()
        val dedupeKey = when {
            sosId.isNotBlank() -> sosId
            !message.messageId.isNullOrBlank() -> message.messageId!!
            else -> ""
        }
        if (!shouldHandleOnce(dedupeKey)) {
            Log.d("FCM_PUSH", "Duplicate SOS push suppressed. key=$dedupeKey")
            return
        }

        if (victimName.isNotBlank()) {
            SosAlertSession.set(
                SosAlertSession.Alert(
                    sosId = if (sosId.isNotBlank()) sosId else "sos-${System.currentTimeMillis()}",
                    victimName = victimName,
                    helpText = helpText,
                    lat = lat,
                    lng = lng,
                )
            )
        }

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "SOS Alert"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Emergency alert received"

        showNotification(
            title = title,
            body = body,
            sosId = sosId,
            victimName = victimName,
            helpText = helpText,
            lat = lat,
            lng = lng,
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        sosId: String,
        victimName: String,
        helpText: String,
        lat: Double?,
        lng: Double?,
    ) {
        val manager = getSystemService<NotificationManager>() ?: return
        createChannelIfNeeded(manager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w("FCM_PUSH", "POST_NOTIFICATIONS permission missing; cannot display push")
                return
            }
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("type", "SOS_ALERT")
            putExtra("sosId", sosId)
            putExtra("victimName", victimName)
            putExtra("helpText", helpText)
            if (lat != null && lng != null) {
                putExtra("lat", lat.toString())
                putExtra("lng", lng.toString())
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createChannelIfNeeded(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        )
        manager.createNotificationChannel(channel)
    }
}


