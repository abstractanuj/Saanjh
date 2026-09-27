package com.geecee.escapelauncher.feature.settings

import android.annotation.SuppressLint
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.feature.homescreen.ContactsViewModel
import com.geecee.escapelauncher.core.data.entity.ContactEntity

@Composable
fun ManageContactsScreen(
    onBack: () -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val contacts by viewModel.allContacts.collectAsState()
    val context = LocalContext.current

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { contactUri ->
            val contentResolver = context.contentResolver
            
            // Query for contact details safely
            val cursor = contentResolver.query(contactUri, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val idIdx = c.getColumnIndex(ContactsContract.Contacts._ID)
                    val nameIdx = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    
                    if (idIdx != -1 && nameIdx != -1) {
                        val id = c.getString(idIdx)
                        val name = c.getString(nameIdx) ?: "Unknown"
                        
                        // Query phone number separately from Data table (more reliable)
                        val phonesCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        
                        var phoneNumber = ""
                        phonesCursor?.use { pCursor ->
                            if (pCursor.moveToFirst()) {
                                val numIdx = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numIdx != -1) {
                                    phoneNumber = pCursor.getString(numIdx) ?: ""
                                }
                            }
                        }
                        
                        if (name.isNotBlank()) {
                            viewModel.addContact(name, phoneNumber)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            EscapeHeader(
                goBack = onBack,
                title = "Manage Contacts"
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { contactPickerLauncher.launch(null) }) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(
                items = contacts,
                key = { it.id }
            ) { contact ->
                ListItem(
                    headlineContent = { Text(contact.name) },
                    supportingContent = { Text(contact.phoneNumber) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteContact(contact.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }
        }
    }
}
