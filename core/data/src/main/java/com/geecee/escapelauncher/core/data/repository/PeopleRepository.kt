package com.geecee.escapelauncher.core.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class PersonContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val lastContactedMillis: Long = 0L,
    val daysSinceLastContact: Int = Int.MAX_VALUE,
    val isOverTwoWeeks: Boolean = true
) {
    fun relativeLastTalkedLabel(): String {
        if (lastContactedMillis <= 0L || daysSinceLastContact == Int.MAX_VALUE) {
            return "2+ weeks ago"
        }
        return when {
            daysSinceLastContact <= 0 -> "today"
            daysSinceLastContact == 1 -> "yesterday"
            daysSinceLastContact < 14 -> "${daysSinceLastContact}d ago"
            daysSinceLastContact < 60 -> "${daysSinceLastContact / 7}w ago"
            else -> "${daysSinceLastContact / 30}mo ago"
        }
    }
}

@Singleton
class PeopleRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavoriteContactsSync(): List<PersonContact> {
        val rawJson = prefs.getString(KEY_FAVORITES_JSON, null) ?: return emptyList()
        val storedList = mutableListOf<PersonContact>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id")
                val name = obj.optString("name")
                val number = obj.optString("phoneNumber")
                if (name.isNotBlank() && number.isNotBlank()) {
                    storedList.add(
                        PersonContact(
                            id = id.ifBlank { number },
                            name = name,
                            phoneNumber = number
                        )
                    )
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }

        if (storedList.isEmpty()) return emptyList()

        val callLogTimestamps = queryLatestCallTimestamps(storedList.map { it.phoneNumber })
        val now = System.currentTimeMillis()

        return storedList.map { contact ->
            val norm = normalizeNumber(contact.phoneNumber)
            val inAppTs = prefs.getLong("$KEY_LAST_CALL_PREFIX$norm", 0L)
            val callLogTs = callLogTimestamps[norm] ?: 0L
            val latestTs = maxOf(inAppTs, callLogTs)
            val days = if (latestTs <= 0L) {
                Int.MAX_VALUE
            } else {
                TimeUnit.MILLISECONDS.toDays((now - latestTs).coerceAtLeast(0L)).toInt()
            }
            val overTwoWeeks = latestTs <= 0L || days >= 14
            contact.copy(
                lastContactedMillis = latestTs,
                daysSinceLastContact = days,
                isOverTwoWeeks = overTwoWeeks
            )
        }.sortedWith(
            compareByDescending<PersonContact> { it.isOverTwoWeeks }
                .thenByDescending { it.daysSinceLastContact }
                .thenBy { it.name.lowercase() }
        )
    }

    /**
     * Returns all selected favorite contacts from settings for the People widget,
     * ordered with people you haven't talked to in 2+ weeks first.
     */
    fun getWidgetPeopleSync(): List<PersonContact> {
        return getFavoriteContactsSync()
    }

    fun toggleFavoriteContact(contact: PersonContact): Boolean {
        val current = getRawFavorites().toMutableList()
        val normTarget = normalizeNumber(contact.phoneNumber)
        val existingIdx = current.indexOfFirst { normalizeNumber(it.phoneNumber) == normTarget }
        val added = if (existingIdx >= 0) {
            current.removeAt(existingIdx)
            false
        } else {
            current.add(contact)
            true
        }
        saveRawFavorites(current)
        notifyWidgetChanged(context)
        return added
    }

    fun recordCallTimestamp(phoneNumber: String) {
        val norm = normalizeNumber(phoneNumber)
        if (norm.isBlank()) return
        prefs.edit().putLong("$KEY_LAST_CALL_PREFIX$norm", System.currentTimeMillis()).apply()
        notifyWidgetChanged(context)
    }

    fun isAwarenessNudgeEnabled(): Boolean {
        return prefs.getBoolean(KEY_AWARENESS_NUDGE_ENABLED, true)
    }

    fun setAwarenessNudgeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AWARENESS_NUDGE_ENABLED, enabled).apply()
    }

    suspend fun getAllDeviceContacts(): List<PersonContact> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext emptyList()
        }

        val list = ArrayList<PersonContact>(64)
        val seen = HashSet<String>(64)
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx) ?: continue
                    val name = cursor.getString(nameIdx)?.trim() ?: continue
                    val number = cursor.getString(numIdx)?.trim() ?: continue
                    if (name.isEmpty() || number.isEmpty()) continue
                    val key = "${name.lowercase()}_${normalizeNumber(number)}"
                    if (seen.add(key)) {
                        list.add(
                            PersonContact(
                                id = id,
                                name = name,
                                phoneNumber = number
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {
        }
        list
    }

    private fun getRawFavorites(): List<PersonContact> {
        val rawJson = prefs.getString(KEY_FAVORITES_JSON, null) ?: return emptyList()
        val list = mutableListOf<PersonContact>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id")
                val name = obj.optString("name")
                val number = obj.optString("phoneNumber")
                if (name.isNotBlank() && number.isNotBlank()) {
                    list.add(PersonContact(id = id, name = name, phoneNumber = number))
                }
            }
        } catch (_: Exception) {
        }
        return list
    }

    private fun saveRawFavorites(contacts: List<PersonContact>) {
        val array = JSONArray()
        contacts.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phoneNumber", c.phoneNumber)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_FAVORITES_JSON, array.toString()).apply()
    }

    private fun queryLatestCallTimestamps(phoneNumbers: List<String>): Map<String, Long> {
        if (phoneNumbers.isEmpty()) return emptyMap()
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptyMap()
        }

        val targetNorms = phoneNumbers.map { normalizeNumber(it) }.filter { it.isNotEmpty() }.toSet()
        if (targetNorms.isEmpty()) return emptyMap()

        val result = HashMap<String, Long>(targetNorms.size)
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )

        try {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                val numIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE)
                val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)

                while (cursor.moveToNext() && result.size < targetNorms.size) {
                    val rawNum = cursor.getString(numIdx) ?: continue
                    val date = cursor.getLong(dateIdx)
                    val type = cursor.getInt(typeIdx)
                    // Consider incoming, outgoing, or connected calls
                    if (type != CallLog.Calls.INCOMING_TYPE &&
                        type != CallLog.Calls.OUTGOING_TYPE
                    ) {
                        continue
                    }
                    val norm = normalizeNumber(rawNum)
                    if (norm in targetNorms && !result.containsKey(norm)) {
                        result[norm] = date
                    }
                }
            }
        } catch (_: Exception) {
        }
        return result
    }

    companion object {
        const val PREFS_NAME = "saanjh_people_prefs"
        private const val KEY_FAVORITES_JSON = "favorites_json"
        private const val KEY_LAST_CALL_PREFIX = "last_call_"
        private const val KEY_AWARENESS_NUDGE_ENABLED = "awareness_nudge_enabled"
        const val ACTION_REFRESH_PEOPLE_WIDGET = "com.sareprne.saanjh.action.REFRESH_PEOPLE_WIDGET"

        fun normalizeNumber(raw: String): String {
            val digits = raw.filter { it.isDigit() }
            return if (digits.length > 10) digits.takeLast(10) else digits
        }

        fun notifyWidgetChanged(context: Context) {
            val intent = Intent(ACTION_REFRESH_PEOPLE_WIDGET).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
        }
    }
}
