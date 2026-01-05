package com.example.guardianlink

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform