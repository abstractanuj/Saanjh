package com.geecee.escapelauncher.feature.settings

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.data.repository.LocalContent
import com.geecee.escapelauncher.core.data.repository.WidgetSettingsRepository
import com.geecee.escapelauncher.core.model.SettingsData
import com.geecee.escapelauncher.core.ui.composables.SettingsGroup
import com.geecee.escapelauncher.core.ui.composables.SettingsDivider
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject
    lateinit var widgetSettingsRepository: WidgetSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isOnboardingComplete = sharedPrefs.getBoolean("isOnboardingComplete", false)
        
        if (!isOnboardingComplete) {
            val intent = Intent().apply {
                setClassName(this@SettingsActivity, "com.geecee.escapelauncher.OnboardingActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(widgetSettingsRepository)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    widgetSettingsRepository: WidgetSettingsRepository,
    viewModel: SettingsActivityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val useDarkText by widgetSettingsRepository.useDarkText.collectAsState(initial = false)
    val settingsData by viewModel.settings.collectAsState(initial = SettingsData())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Escape Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 0.dp, vertical = 8.dp),
        ) {
            
            SettingsGroup(title = "Customization") {
                SettingsNavigationItem(
                    label = "Icon Pack",
                    onClick = {
                        context.startActivity(Intent(context, IconPackSettingsActivity::class.java))
                    }
                )
                SettingsNavigationItem(
                    label = "Set as Default Launcher",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val rm = context.getSystemService(android.app.role.RoleManager::class.java)
                            if (rm?.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) == true && !rm.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)) {
                                val intent = rm.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
                                context.startActivity(intent)
                                return@SettingsNavigationItem
                            }
                        }
                        context.startActivity(Intent(android.provider.Settings.ACTION_HOME_SETTINGS))
                    }
                )
                SettingsDivider()
                SettingsSwitch(
                    label = "Widget Text Color",
                    description = "Use dark text for better contrast on widgets",
                    checked = useDarkText,
                    onCheckedChange = { checked ->
                        scope.launch {
                            widgetSettingsRepository.setUseDarkText(checked)
                        }
                    }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    label = "Configure Gestures",
                    onClick = {
                        val intent = Intent("com.geecee.escapelauncher.action.GESTURE_SETUP").apply {
                            setPackage(context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = "Add Quick Settings Tile",
                        onClick = {
                            val statusBarManager = context.getSystemService(StatusBarManager::class.java)
                            statusBarManager?.requestAddTileService(
                                ComponentName(
                                    context.packageName,
                                    "com.geecee.escapelauncher.feature.gestures.GestureTileService"
                                ),
                                "Gesture Pad",
                                android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_menu_edit),
                                { it.run() },
                                { result ->
                                    // Optional callback handling
                                }
                            )
                        }
                    )
                }
            }

            SettingsGroup(title = "Glassmorphic Reminders") {
                SettingsNavigationItem(
                    label = "Frequency (Current: ${settingsData.reminderFrequencyHours} hours)",
                    onClick = {
                        val newFreq = if (settingsData.reminderFrequencyHours < 12) settingsData.reminderFrequencyHours + 1 else 1
                        viewModel.updateReminderFrequency(newFreq)
                    }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    label = "Time Window (${settingsData.reminderStartTime} - ${settingsData.reminderEndTime})",
                    onClick = {
                        if (settingsData.reminderStartTime == "08:00") {
                            viewModel.updateReminderTimeWindow("00:00", "23:59")
                        } else {
                            viewModel.updateReminderTimeWindow("08:00", "22:00")
                        }
                    }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    label = "Preview Reminder",
                    onClick = {
                        val quote = LocalContent.inspirationalQuotes.random()
                        val intent = Intent().apply {
                            setClassName(context, "com.geecee.escapelauncher.GlassmorphicReminderActivity")
                            putExtra("REMINDER_TEXT", quote)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                )
            }

            SettingsGroup(title = "Recommendations") {
                SettingsNavigationItem(
                    label = "Recommendation Frequency (${settingsData.recommendationFrequencyHours} hours)",
                    onClick = {
                        val newFreq = if (settingsData.recommendationFrequencyHours < 48) settingsData.recommendationFrequencyHours + 6 else 6
                        viewModel.updateRecommendationFrequency(newFreq)
                    }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    label = "Preview Recommendations",
                    onClick = {
                        com.geecee.escapelauncher.core.data.worker.RecommendationWorker.showRecommendationPreview(context)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
