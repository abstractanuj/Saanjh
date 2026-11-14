package com.geecee.escapelauncher.feature.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.theme.EscapeTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IconPackInfo(val name: String, val packageName: String?)

@HiltViewModel
class IconPackSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val selectedIconPack = settingsRepository.iconPackPackage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setIconPack(packageName: String?) {
        viewModelScope.launch {
            settingsRepository.setIconPackPackage(packageName)
        }
    }
}

@AndroidEntryPoint
class IconPackSettingsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val pm = packageManager
        val themes = mutableListOf<IconPackInfo>()

        // Find installed icon packs
        val intent = Intent("com.novalauncher.THEME")
        val resolves = pm.queryIntentActivities(intent, 0)
        for (resolve in resolves) {
            val appInfo = resolve.activityInfo.applicationInfo
            themes.add(IconPackInfo(pm.getApplicationLabel(appInfo).toString(), appInfo.packageName))
        }

        setContent {
            EscapeTheme {
                val viewModel: IconPackSettingsViewModel = hiltViewModel()
                val selectedPack by viewModel.selectedIconPack.collectAsState()

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Icon Pack") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        item {
                            IconPackRow(
                                name = "System Default",
                                isSelected = selectedPack == null,
                                onClick = { viewModel.setIconPack(null) }
                            )
                        }

                        items(themes, key = { it.packageName!! }) { theme ->
                            IconPackRow(
                                name = theme.name,
                                isSelected = selectedPack == theme.packageName,
                                onClick = { viewModel.setIconPack(theme.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IconPackRow(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}
