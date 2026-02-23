@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package contacts

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.DeviceContact

actual object DeviceContactsHelper {

    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context

    /** Call once from [MainActivity.onCreate]. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    actual suspend fun fetchContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DeviceContact>()
        val resolver = appContext.contentResolver

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )

        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIdx) ?: continue
                val phone = cursor.getString(phoneIdx) ?: continue
                result.add(DeviceContact(name = name, phone = phone.replace("\\s".toRegex(), "")))
            }
        }

        // De-duplicate by (name, phone) and sort
        result.distinctBy { "${it.name}|${it.phone}" }
            .sortedBy { it.name.lowercase() }
    }
}
