package util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

actual fun nowTimestampText(): String {
    val formatter = NSDateFormatter()
    formatter.locale = NSLocale.currentLocale
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
    return formatter.stringFromDate(NSDate())
}

