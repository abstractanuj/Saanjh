package com.geecee.escapelauncher.feature.settings.mainpage

import android.Manifest
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.*
import com.geecee.escapelauncher.feature.settings.SettingsNavKey
import com.geecee.escapelauncher.core.common.EscapeAccessibilityService
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.graphics.drawable.Icon

/**
 * First page of settings, contains navigation to all the other pages
 */
@Composable
fun MainSettingsPage(
    goBack: () -> Unit,
    onNavigate: (SettingsNavKey) -> Unit,
    mainSettingsPageViewModel: MainSettingsPageViewModel = hiltViewModel()
) {
    val uiState by mainSettingsPageViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Handle case where permission cannot be made persistable
                }
                mainSettingsPageViewModel.setPeoplePageBackgroundUri(uri.toString())
            }
        }
    )

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                mainSettingsPageViewModel.exportBackup { json ->
                    context.contentResolver.openOutputStream(uri)?.use { 
                        it.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Settings exported successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val json = stream.bufferedReader().use { it.readText() }
                    mainSettingsPageViewModel.importBackup(json) {
                        Toast.makeText(context, "Settings imported successfully. Restarting...", Toast.LENGTH_LONG).show()
                        // Restart app or just refresh
                        // For now, just toast. Ideally we might want to restart to ensure all components refresh.
                    }
                }
            }
        }
    )

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                mainSettingsPageViewModel.setShowLauncherSwitcherNotification(true)
            } else {
                Toast.makeText(context, "Notification permission required for this feature", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        item(key = "header") {
            EscapeHeader(
                goBack, stringResource(R.string.settings)
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Look & Feel
                SettingsGroup(title = stringResource(R.string.general)) {
                    SettingsNavigationItem(
                        label = stringResource(id = R.string.theme),
                        onClick = { onNavigate(SettingsNavKey.Theme) }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = stringResource(id = R.string.choose_font),
                        onClick = { onNavigate(SettingsNavKey.ChooseFont) }
                    )
                }

                // Apps List
                SettingsGroup(title = stringResource(R.string.apps)) {
                    SettingsSwitch(
                        label = stringResource(id = R.string.search_at_bottom),
                        checked = uiState.bottomSearch,
                        onCheckedChange = { mainSettingsPageViewModel.setBottomSearch(it) }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = "Manage Folders",
                        onClick = { onNavigate(SettingsNavKey.ManageFolders) }
                    )
                }

                // Gestures
                SettingsGroup(title = "Gestures") {
                    SettingsSwitch(
                        label = "Enable Gestures",
                        checked = uiState.gestureNavigation,
                        onCheckedChange = { mainSettingsPageViewModel.setGestureNavigation(it) }
                    )
                    Text(
                        text = "Swipe from middle to go home. Swipe up from bottom for Recents. Swipe from sides for Back.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                // People Page
                SettingsGroup(title = "People Page") {
                    SettingsNavigationItem(
                        label = "Choose Background Image",
                        onClick = { 
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = "Clear Background Image",
                        onClick = { mainSettingsPageViewModel.setPeoplePageBackgroundUri(null) }
                    )
                }

                // Today's Picks
                SettingsGroup(title = "Today's Picks") {
                    SettingsNavigationItem(
                        label = "Custom YouTube Channels",
                        onClick = { onNavigate(SettingsNavKey.CustomYoutubeChannels) }
                    )
                }

                // Media Controls
                SettingsGroup(title = "Media Controls") {
                    SettingsSwitch(
                        label = "Enable Now Playing Card",
                        checked = uiState.enableNowPlaying,
                        onCheckedChange = { mainSettingsPageViewModel.setEnableNowPlaying(it) }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = "Grant Notification Access",
                        onClick = {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    )
                    Text(
                        text = "Notification access is required to show current media controls on the home screen.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                // Advanced
                SettingsGroup(title = stringResource(R.string.advanced)) {
                    SettingsSwitch(
                        label = "Launcher Switcher Shortcut (Notification)",
                        checked = uiState.showLauncherSwitcherNotification,
                        onCheckedChange = { isChecked ->
                            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Request permission on Android 13+
                                val hasPermission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    mainSettingsPageViewModel.setShowLauncherSwitcherNotification(true)
                                } else {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                mainSettingsPageViewModel.setShowLauncherSwitcherNotification(isChecked)
                            }
                        }
                    )
                    Text(
                        text = "Provides a persistent shortcut in your notification drawer to switch default launchers seamlessly.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        SettingsDivider()
                        SettingsNavigationItem(
                            label = "Add Launcher Switcher Quick Tile",
                            onClick = {
                                val statusBarManager = context.getSystemService(StatusBarManager::class.java)
                                val componentName = ComponentName(
                                    context.packageName,
                                    "com.geecee.escapelauncher.LauncherTileService"
                                )
                                statusBarManager.requestAddTileService(
                                    componentName,
                                    "Switch Launcher",
                                    Icon.createWithResource(context, R.drawable.launcher_logo_icon),
                                    context.mainExecutor,
                                    { _ ->
                                        // Handle result if necessary
                                    }
                                )
                            }
                        )
                    }

                    SettingsDivider()
                    SettingsNavigationItem(
                        label = "Manage Contacts",
                        onClick = { onNavigate(SettingsNavKey.ManageContacts) }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = stringResource(R.string.manage_favourite_apps),
                        onClick = { onNavigate(SettingsNavKey.BulkFavouriteApps) }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = stringResource(R.string.manage_hidden_apps),
                        onClick = { onNavigate(SettingsNavKey.HiddenApps) }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = stringResource(R.string.manage_open_challenges),
                        onClick = { onNavigate(SettingsNavKey.OpenChallenges) }
                    )
                }

                // Backup & Restore
                SettingsGroup(title = "Backup & Restore") {
                    SettingsNavigationItem(
                        label = "Export Settings",
                        onClick = { exportLauncher.launch("escape_launcher_backup.json") }
                    )
                    SettingsDivider()
                    SettingsNavigationItem(
                        label = "Import Settings",
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
