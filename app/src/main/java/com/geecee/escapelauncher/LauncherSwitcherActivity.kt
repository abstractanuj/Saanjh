package com.geecee.escapelauncher

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LauncherSwitcherActivity : ComponentActivity() {

    private var hasChosenLauncher = false

    private val roleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            hasChosenLauncher = true
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(homeIntent)
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Immediately trigger the system prompt when the activity starts
        triggerRoleManager()
        
        setContent {
            MaterialTheme {
                if (hasChosenLauncher) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .scale(scale)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha), shape = RoundedCornerShape(24.dp))
                            )
                            
                            Spacer(modifier = Modifier.height(48.dp))
                            
                            Text(
                                text = "Transitioning Launcher...",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Take a deep breath.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    LaunchedEffect(hasChosenLauncher) {
                        delay(10000) // 10 second box breathing delay
                        finish() // End activity to reveal the new launcher underneath
                    }
                }
            }
        }
    }

    private fun triggerRoleManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                    // If Escape Launcher is ALREADY the default, Android will NOT show a popup.
                    // It will just silently return OK. To let the user pick a different launcher,
                    // we are forced to open the full-screen system settings page.
                    val fallbackIntent = Intent(Settings.ACTION_HOME_SETTINGS)
                    startActivity(fallbackIntent)
                    finish()
                } else {
                    // If Escape is NOT the default, we can use the system popup
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                    roleLauncher.launch(intent)
                }
                return
            }
        }
        
        val fallbackIntent = Intent(Settings.ACTION_HOME_SETTINGS)
        startActivity(fallbackIntent)
        finish()
    }
}
