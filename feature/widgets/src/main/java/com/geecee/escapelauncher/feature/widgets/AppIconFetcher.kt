package com.geecee.escapelauncher.feature.widgets

import android.content.Context
import android.content.pm.LauncherApps
import coil3.ImageLoader
import coil3.asImage
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import com.geecee.escapelauncher.core.data.repository.IconPackManager
import com.geecee.escapelauncher.core.model.InstalledApp

class AppIconFetcher(
    private val app: InstalledApp,
    private val options: Options,
    private val context: Context,
    private val iconPackManager: IconPackManager
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val defaultDrawable = try {
            launcherApps.getActivityList(app.packageName, app.user).firstOrNull()?.getBadgedIcon(0)
        } catch (_: Exception) {
            null
        }

        val icon = iconPackManager.getIconForPackage(app.packageName, defaultDrawable)

        return icon?.let {
            ImageFetchResult(
                image = it.asImage(),
                isSampled = false,
                dataSource = coil3.decode.DataSource.MEMORY
            )
        }
    }

    class Factory(
        private val context: Context,
        private val iconPackManager: IconPackManager
    ) : Fetcher.Factory<InstalledApp> {
        override fun create(
            data: InstalledApp,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher {
            return AppIconFetcher(data, options, context, iconPackManager)
        }
    }
}