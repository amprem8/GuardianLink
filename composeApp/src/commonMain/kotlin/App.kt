package com.example.guardianlink

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import screens.SplashScreenActivity

@Composable
fun App() {
    MaterialTheme { Navigator(SplashScreenActivity()) }
}
