package screens

import android.annotation.SuppressLint
import android.content.Context
import java.io.File

@SuppressLint("StaticFieldLeak")
private lateinit var appCtx: Context

fun initTriggerConfigPlatform(context: Context) {
    appCtx = context.applicationContext
}

actual fun tempAudioFilePath(): String {
    val dir = appCtx.cacheDir
    val file = File(dir, "voice_phrase_${System.currentTimeMillis()}.m4a")
    return file.absolutePath
}

