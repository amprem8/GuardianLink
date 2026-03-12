package com.example.guardianlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import audio.VoicePhraseRecorder
import contacts.DeviceContactsHelper
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
        NetworkConnectivityObserver.init(this)
        NetworkConnectivityObserver.start()
        initTriggerConfigPlatform(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}