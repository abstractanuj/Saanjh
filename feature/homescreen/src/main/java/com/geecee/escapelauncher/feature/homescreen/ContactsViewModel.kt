package com.geecee.escapelauncher.feature.homescreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.database.ContactDao
import com.geecee.escapelauncher.core.data.entity.ContactEntity
import com.geecee.escapelauncher.core.data.repository.CallLogRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.CallLogEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class PeopleTile {
    data class Contact(val contact: ContactEntity, val lastCall: CallLogEntry? = null) : PeopleTile()
    data class Missed(val call: CallLogEntry) : PeopleTile()
}

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactDao: ContactDao,
    private val callLogRepository: CallLogRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val allContacts: StateFlow<List<ContactEntity>> = contactDao.getAllContacts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val lastCallDetails: StateFlow<Map<String, CallLogEntry>> = allContacts
        .flatMapLatest { contacts ->
            val numbers = contacts.map { it.phoneNumber }
            callLogRepository.getLastCallDetails(numbers)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val missedCalls: StateFlow<List<CallLogEntry>> = callLogRepository.getMissedCalls(12)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val peoplePageBackgroundUri: StateFlow<String?> = settingsRepository.peoplePageBackgroundUri
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val peopleTiles: StateFlow<List<PeopleTile>> = combine(allContacts, missedCalls, lastCallDetails) { contacts, calls, details ->
        val result = mutableListOf<PeopleTile>()
        
        contacts.take(25).forEach { contact -> 
            val detail = details[contact.phoneNumber]
            result.add(PeopleTile.Contact(contact, detail)) 
        }
        
        calls.filter { call -> 
            val normalizedNum = call.number.replace("\\s".toRegex(), "").takeLast(10)
            contacts.none { it.phoneNumber.replace("\\s".toRegex(), "").takeLast(10) == normalizedNum }
        }.take(20).forEach { result.add(PeopleTile.Missed(it)) }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setBackgroundUri(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setPeoplePageBackgroundUri(uri)
        }
    }

    fun addContact(name: String, phoneNumber: String) {

        viewModelScope.launch {
            try {
                val colors = listOf(
                    0xFF00ABA9.toInt(), 0xFF2D89EF.toInt(), 0xFF603CBA.toInt(),
                    0xFFFF0097.toInt(), 0xFF9F00A7.toInt(), 0xFF7E3878.toInt(),
                    0xFF1D1D1D.toInt(), 0xFF00A300.toInt(), 0xFFE3A21A.toInt(), 0xFFDA532C.toInt()
                )
                
                // Auto-size logic based on Metro distribution
                val random = (0..99).random()
                val size = when {
                    random < 60 -> "Small"
                    random < 85 -> "Medium" // Wide
                    else -> "Large"
                }
                
                // Rhythm: Use name hash to pick color so it's consistent but looks varied
                val color = colors[name.hashCode().let { if (it < 0) -it else it } % colors.size]
                
                contactDao.insertContact(
                    ContactEntity(
                        name = name,
                        phoneNumber = phoneNumber,
                        tileColor = color,
                        tileSize = size
                    )
                )

            } catch (e: Exception) {
                Timber.tag("Contacts").e(e, "Failed to add contact: $name")
            }
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            contactDao.deleteContact(id)
        }
    }

    @SuppressLint("MissingPermission")
    fun callContact(context: Context, phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to dialer if call fails (e.g. permission denied at runtime)
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
        }
    }
}
