package util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual fun nowTimestampText(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return LocalDateTime.now().format(formatter)
}

