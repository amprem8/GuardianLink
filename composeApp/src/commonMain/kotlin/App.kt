package com.example.guardianlink

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import screens.SosAlertDetailsActivity
import screens.SplashScreenActivity
import session.SosAlertSession

@Composable
fun App() {
    MaterialTheme {
        Navigator(SplashScreenActivity()) { navigator ->
            val alert by SosAlertSession.pendingAlert.collectAsState()
            var lastOpenedAlertId by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(alert?.sosId) {
                val currentId = alert?.sosId
                if (currentId != null && currentId != lastOpenedAlertId) {
                    lastOpenedAlertId = currentId
                    navigator.push(SosAlertDetailsActivity())
                }
                if (currentId == null) {
                    lastOpenedAlertId = null
                }
            }

            CurrentScreen()
        }
    }
}
