package com.geecee.escapelauncher.core.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.geecee.escapelauncher.core.model.ContactData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun searchContacts(query: String): List<ContactData> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext emptyList()
        }

        val contacts = mutableListOf<ContactData>()
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")
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

                while (cursor.moveToNext() && contacts.size < 10) { // limit results
                    val id = cursor.getString(idIndex)
                    val name = cursor.getString(nameIndex)
                    val number = cursor.getString(numberIndex)
                    val hasWhatsapp = checkHasWhatsapp(id)
                    contacts.add(ContactData(id, name, number, hasWhatsapp))
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        contacts.distinctBy { it.name }
    }

    private fun checkHasWhatsapp(contactId: String): Boolean {
        var hasWhatsapp = false
        val projection = arrayOf(ContactsContract.Data._ID)
        val selection = "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(contactId, "vnd.android.cursor.item/vnd.com.whatsapp.profile")

        try {
            context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    hasWhatsapp = true
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return hasWhatsapp
    }
}
