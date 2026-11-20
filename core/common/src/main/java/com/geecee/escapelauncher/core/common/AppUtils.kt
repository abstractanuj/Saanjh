package com.geecee.escapelauncher.core.common

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Process
import androidx.core.net.toUri
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.normalizeSearchText

/**
 * Checks if the app belongs to the main user.
 */
fun InstalledApp.isMainUserApp(): Boolean = this.user == Process.myUserHandle()

/**
 * Function to launch an app.
 */
fun launchApp(
    context: Context,
    app: InstalledApp,
    onAppOpened: ((String) -> Unit)? = null
): Boolean {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val options = ActivityOptions.makeBasic()

    return try {
        launcherApps.startMainActivity(
            app.componentName,
            app.user,
            Rect(),
            options.toBundle()
        )
        onAppOpened?.invoke(app.packageName)
        true
    } catch (e: SecurityException) {
        AppLogger.e("SecurityException opening app: ${e.message}", e, tag = "AppUtils")
        launchViaIntentFallback(context, app, onAppOpened)
    } catch (e: Exception) {
        AppLogger.e("Error opening app", e, tag = "AppUtils")
        false
    }
}

private fun launchViaIntentFallback(
    context: Context,
    app: InstalledApp,
    onAppOpened: ((String) -> Unit)?
): Boolean {
    return try {
        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (intent != null) {
            context.startActivity(intent)
            onAppOpened?.invoke(app.packageName)
            true
        } else {
            false
        }
    } catch (fallbackException: Exception) {
        AppLogger.e("Failed to launch app even with fallback", fallbackException, tag = "AppUtils")
        false
    }
}

/**
 * Filters and ranks apps by mathematical affinity score to [query].
 */
fun sortAppsByRelevance(apps: List<InstalledApp>, query: String): List<InstalledApp> {
    val normalizedQuery = normalizeSearchText(query.trim())
    if (normalizedQuery.isEmpty()) return apps

    return apps.asSequence()
        .map { app -> app to scoreInstalledApp(app, normalizedQuery) }
        .filter { it.second >= 0 }
        .sortedWith(
            compareByDescending<Pair<InstalledApp, Int>> { it.second }
                .thenBy { it.first.normalizedName }
        )
        .map { it.first }
        .toList()
}

fun uninstallApp(context: Context, app: InstalledApp) {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = "package:${app.packageName}".toUri()
        putExtra(Intent.EXTRA_USER, app.user)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun goToAppInfo(
    context: Context,
    app: InstalledApp,
    sourceBounds: Rect? = null
) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val profile = app.user

    val options = ActivityOptions.makeBasic()
    if (sourceBounds != null) {
        options.launchBounds = sourceBounds
    }
    launcherApps.startAppDetailsActivity(
        app.componentName,
        profile,
        sourceBounds,
        options.toBundle()
    )
}