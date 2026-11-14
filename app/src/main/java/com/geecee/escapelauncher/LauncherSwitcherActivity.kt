package com.geecee.escapelauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class LauncherSwitcherActivity : ComponentActivity() {

    private var dialogShownTime = 0L

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val elapsed = System.currentTimeMillis() - dialogShownTime
        // If it returned almost instantly, the system bypassed the UI because we're already default
        // In that case, the only reliable way to switch defaults away from us is opening the Settings screen
        if (elapsed < 500L) {
            val settingsIntent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(settingsIntent)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Try to clear our own preferred activity so RoleManager sees us as not holding the role
        @Suppress("DEPRECATION")
        packageManager.clearPackagePreferredActivities(packageName)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(android.app.role.RoleManager::class.java)
            if (roleManager?.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) == true) {
                dialogShownTime = System.currentTimeMillis()
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
                roleLauncher.launch(intent)
                return
            }
        }
        
        // Fallback for older devices or if RoleManager is not available
        val settingsIntent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(settingsIntent)
        finish()
    }
}
