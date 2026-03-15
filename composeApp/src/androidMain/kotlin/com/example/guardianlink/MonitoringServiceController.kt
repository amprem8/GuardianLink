package com.example.guardianlink

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import storage.AppStorage

object MonitoringServiceController {

    private const val TAG = "MonitoringService"

    fun syncWithStoredPreference(context: Context) {
        val enabled = AppStorage.isContinuousMonitoring()
        applyContinuousMonitoring(context, enabled)
    }

    fun applyContinuousMonitoring(context: Context, enabled: Boolean) {
        if (enabled) {
            startMonitoringService(context)
        } else {
            stopMonitoringService(context)
        }
    }

    private fun startMonitoringService(context: Context) {
        val appContext = context.applicationContext
        val serviceIntent = Intent(appContext, SOSForegroundService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(serviceIntent)
            } else {
                appContext.startService(serviceIntent)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "Unable to start foreground monitoring service due to missing runtime permission", e)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Unable to start foreground monitoring service in current app state", e)
        }
    }

    private fun stopMonitoringService(context: Context) {
        val appContext = context.applicationContext
        appContext.stopService(Intent(appContext, SOSForegroundService::class.java))
    }
}

