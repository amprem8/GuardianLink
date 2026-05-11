package com.example.guardianlink

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import storage.AppStorage

/**
 * Restores continuous monitoring after reboot/app update so protection remains active
 * even before the user manually opens the app.
 */
class MonitoringBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                AppStorage.init(context)
                MonitoringServiceController.syncWithStoredPreference(context)
            }
        }
    }
}

