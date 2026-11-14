package com.geecee.escapelauncher.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.widgetSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_settings")

@Singleton
class WidgetSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val PREF_USE_DARK_TEXT = booleanPreferencesKey("use_dark_text")

    val useDarkText: Flow<Boolean> = context.widgetSettingsDataStore.data
        .map { preferences ->
            preferences[PREF_USE_DARK_TEXT] ?: false
        }

    suspend fun setUseDarkText(useDark: Boolean) {
        context.widgetSettingsDataStore.edit { preferences ->
            preferences[PREF_USE_DARK_TEXT] = useDark
        }
    }
}