package com.geecee.escapelauncher

import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.activity.result.contract.ActivityResultContracts
import com.geecee.escapelauncher.feature.settings.SettingsActivity

class OnboardingActivity : ComponentActivity() {

    private fun finishOnboarding() {
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("isOnboardingComplete", true).apply()

        val intent = Intent(this, SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        finishOnboarding()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    OnboardingScreen(
                        onFinish = {
                            finishOnboarding()
                        },
                        onRequestDefault = {
                            requestDefaultLauncher()
                        }
                    )
                }
            }
        }
    }
    
    private fun requestDefaultLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = getSystemService(RoleManager::class.java)
            if (rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        // Fallback for Android 9 and below
        startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        finishOnboarding()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit, onRequestDefault: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    var hasUsageAccess by remember { mutableStateOf(checkUsageAccess(context)) }
    var hasNotificationAccess by remember { mutableStateOf(checkNotificationAccess(context)) }

    // Re-check permissions when returning to this screen
    LaunchedEffect(pagerState.currentPage) {
        hasUsageAccess = checkUsageAccess(context)
        hasNotificationAccess = checkNotificationAccess(context)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false // Force users to click buttons
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                when (page) {
                    0 -> {
                        Text(
                            text = "Welcome to Escape",
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "A minimalist tool designed to help you regain focus and reduce screen time.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { scope.launch { pagerState.animateScrollToPage(1) } }) {
                            Text("Get Started")
                        }
                    }
                    1 -> {
                        Text(
                            text = "Usage Access",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "We need this to track screen time and enforce your app limits. This data never leaves your device.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        if (hasUsageAccess) {
                            Text("Granted!", color = Color.Green)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { scope.launch { pagerState.animateScrollToPage(2) } }) {
                                Text("Next")
                            }
                        } else {
                            Button(onClick = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }) {
                                Text("Grant Permission")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                hasUsageAccess = checkUsageAccess(context)
                                if (hasUsageAccess) scope.launch { pagerState.animateScrollToPage(2) }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                                Text("I've granted it")
                            }
                        }
                    }
                    2 -> {
                        Text(
                            text = "Notification Access",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Required to show notification badges and filter out distracting pings.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        if (hasNotificationAccess) {
                            Text("All set!", color = Color.Green)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { 
                                onRequestDefault()
                            }) {
                                Text("Finish Setup")
                            }
                        } else {
                            Button(onClick = {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            }) {
                                Text("Grant Permission")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                hasNotificationAccess = checkNotificationAccess(context)
                                if (hasNotificationAccess) onFinish()
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                                Text("I've granted it")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun checkUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        @Suppress("DEPRECATION")
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName)
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun checkNotificationAccess(context: Context): Boolean {
    val contentResolver = context.contentResolver
    val enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    return enabledNotificationListeners?.contains(context.packageName) == true
}
