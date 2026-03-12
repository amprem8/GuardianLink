package com.example.guardianlink

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import audio.VoicePhraseRecorder
import contacts.DeviceContactsHelper
import gesture.GestureDetectionEngine
import network.NetworkConnectivityObserver
import screens.initTriggerConfigPlatform
import storage.AppStorage
import storage.ContactStorage
import storage.TriggerConfigStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppStorage.init(this)
        ContactStorage.init(this)
        DeviceContactsHelper.init(this)
        TriggerConfigStorage.init(this)
        VoicePhraseRecorder.init(this)
        GestureDetectionEngine.init(this)
        NetworkConnectivityObserver.init(this)
        NetworkConnectivityObserver.start()
        initTriggerConfigPlatform(this)
        MonitoringServiceController.syncWithStoredPreference(this)

        setContent {
            App()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.action == KeyEvent.ACTION_DOWN) {
            GestureDetectionEngine.notifyVolumeDownPress()
        }
        return super.dispatchKeyEvent(event)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}