package com.geecee.escapelauncher.core.common

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Extension function to check if a specific permission is granted
 *
 * @param permission The permission string to check
 * @return True if granted, false otherwise
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Extension function to request a specific permission if it hasn't been granted
 *
 * @param permission The permission string to request
 * @param requestCode The request code to use
 */
fun Activity.requestPermission(permission: String, requestCode: Int) {
    if (!hasPermission(permission)) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            requestCode
        )
    }
}

/**
 * Checks if the Notification Listener service is enabled for this app.
 */
fun Context.isNotificationListenerEnabled(): Boolean {
    val packageName = packageName
    val flat = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    if (flat != null) {
        val names = flat.split(":")
        for (name in names) {
            val componentName = android.content.ComponentName.unflattenFromString(name)
            if (componentName != null && componentName.packageName == packageName) {
                return true
            }
        }
    }
    return false
}
