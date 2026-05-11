package com.example.guardianlink

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.ComposeUIViewController
import network.NetworkConnectivityObserver

fun MainViewController() = ComposeUIViewController {
    DisposableEffect(Unit) {
        NetworkConnectivityObserver.start()
        onDispose { NetworkConnectivityObserver.stop() }
    }
    App()
}