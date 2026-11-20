package com.geecee.escapelauncher.core.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.geecee.escapelauncher.core.model.ContactData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /**
     * Searches contacts and resolves WhatsApp availability in a single batched ContentResolver
     * query (eliminating the previous N+1 IPC query bottleneck).
     */
    suspend fun searchContacts(query: String): List<ContactData> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext emptyList()
        }

        val rawMatches = ArrayList<Triple<String, String, String>>(12)
        val seenNames = HashSet<String>(12)
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$trimmed%")
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext() && rawMatches.size < 10) {
                    val id = cursor.getString(idIndex) ?: continue
                    val name = cursor.getString(nameIndex) ?: continue
                    val number = cursor.getString(numberIndex) ?: continue
                    if (seenNames.add(name)) {
                        rawMatches.add(Triple(id, name, number))
                    }
                }
            }
        } catch (_: Exception) {
            return@withContext emptyList()
        }

        if (rawMatches.isEmpty()) return@withContext emptyList()

        val whatsappIds = fetchWhatsAppContactIdsBatch(rawMatches.map { it.first })
        rawMatches.map { (id, name, number) ->
            ContactData(
                id = id,
                name = name,
                phoneNumber = number,
                hasWhatsApp = whatsappIds.contains(id)
            )
        }
    }

    private fun fetchWhatsAppContactIdsBatch(contactIds: List<String>): Set<String> {
        if (contactIds.isEmpty()) return emptySet()
        val placeholders = contactIds.joinToString(",") { "?" }
        val projection = arrayOf(ContactsContract.Data.CONTACT_ID)
        val selection = "${ContactsContract.Data.CONTACT_ID} IN ($placeholders) AND ${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = (contactIds + "vnd.android.cursor.item/vnd.com.whatsapp.profile").toTypedArray()

        val result = HashSet<String>(contactIds.size)
        try {
            context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
                while (cursor.moveToNext()) {
                    cursor.getString(idIdx)?.let { result.add(it) }
                }
            }
        } catch (_: Exception) {
        }
        return result
    }
}
