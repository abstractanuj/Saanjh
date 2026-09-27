package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.data.database.ContactDao
import com.geecee.escapelauncher.core.data.database.ModifiedAppsDao
import com.geecee.escapelauncher.core.data.entity.ContactEntity
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.SettingsData
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import jakarta.inject.Inject
import jakarta.inject.Singleton

data class BackupData(
    val settings: SettingsData,
    val modifiedApps: List<ModifiedAppEntity>,
    val contacts: List<ContactEntity>
)

@Singleton
class BackupRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val modifiedAppsDao: ModifiedAppsDao,
    private val contactDao: ContactDao,
    private val gson: Gson
) {
    suspend fun exportData(): String {
        val settings = settingsRepository.settings.first()
        val allModifiedApps = modifiedAppsDao.getAllModifiedApps()
        val allContacts = contactDao.getAllContacts().first()
        
        val backupData = BackupData(
            settings = settings,
            modifiedApps = allModifiedApps,
            contacts = allContacts
        )
        
        return gson.toJson(backupData)
    }

    suspend fun importData(json: String) {
        val backupData = gson.fromJson(json, BackupData::class.java)
        
        // Update Settings
        settingsRepository.updateSettings(backupData.settings)
        
        // Update Modified Apps
        modifiedAppsDao.upsertAll(backupData.modifiedApps)
        
        // Update Contacts (Clean restore)
        contactDao.deleteAllContacts()
        backupData.contacts.forEach { 
            // Reset ID to 0 to allow Room to auto-generate if it conflicts, 
            // but here we probably want to keep the IDs if we just cleared the table.
            // Actually, keep them as is since we just cleared.
            contactDao.insertContact(it) 
        }
    }
}
