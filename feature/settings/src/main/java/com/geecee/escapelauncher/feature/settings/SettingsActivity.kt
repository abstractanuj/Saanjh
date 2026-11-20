package com.geecee.escapelauncher.feature.settings

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.data.repository.PeopleRepository
import com.geecee.escapelauncher.core.data.repository.WidgetSettingsRepository
import com.geecee.escapelauncher.core.ui.composables.SettingsDivider
import com.geecee.escapelauncher.core.ui.composables.SettingsGroup
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject
    lateinit var widgetSettingsRepository: WidgetSettingsRepository

    @Inject
    lateinit var peopleRepository: PeopleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(
                        widgetSettingsRepository = widgetSettingsRepository,
                        peopleRepository = peopleRepository
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    widgetSettingsRepository: WidgetSettingsRepository,
    peopleRepository: PeopleRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val useDarkText by widgetSettingsRepository.useDarkText.collectAsState(initial = false)
    var awarenessNudgeEnabled by remember {
        mutableStateOf(peopleRepository.isAwarenessNudgeEnabled())
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("saanjh") },
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
                .padding(vertical = 8.dp)
        ) {
            SettingsGroup(title = "Customization, Settings & Setup") {
                SettingsNavigationItem(
                    label = "People (2+ Weeks & Favorite Contacts)",
                    onClick = {
                        context.startActivity(Intent(context, FavoriteContactsActivity::class.java))
                    }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    label = "Configure Gestures (Apps & Contacts)",
                    onClick = {
                        val intent = Intent("com.geecee.escapelauncher.action.GESTURE_SETUP").apply {
                            setPackage(context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
                SettingsDivider()
                SettingsSwitch(
                    label = "Awareness Nudge",
                    description = "Periodic 6-second top pill with a short grounding prompt",
                    checked = awarenessNudgeEnabled,
                    onCheckedChange = { checked ->
                        awarenessNudgeEnabled = checked
                        peopleRepository.setAwarenessNudgeEnabled(checked)
                    }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    label = "Show Awareness Nudge Now",
                    onClick = {
                        val intent = Intent("com.sareprne.saanjh.action.AWARENESS_NUDGE").apply {
                            setPackage(context.packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        }
                        context.startActivity(intent)
                    }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    label = "Icon Pack",
                    onClick = {
                        context.startActivity(Intent(context, IconPackSettingsActivity::class.java))
                    }
                )
                SettingsDivider()
                SettingsNavigationItem(
                    label = "Set as Default Launcher",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val rm = context.getSystemService(android.app.role.RoleManager::class.java)
                            if (rm?.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) == true &&
                                !rm.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)
                            ) {
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
                                android.graphics.drawable.Icon.createWithResource(
                                    context,
                                    android.R.drawable.ic_menu_edit
                                ),
                                { it.run() },
                                {}
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
