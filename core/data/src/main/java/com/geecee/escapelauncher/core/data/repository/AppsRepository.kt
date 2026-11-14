package com.geecee.escapelauncher.core.data.repository

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import com.geecee.escapelauncher.core.di.ApplicationScope
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.common.AppLogger
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.supervisorScope
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Singleton
class AppsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val scope: CoroutineScope,
    private val iconColorCache: IconColorCache,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    val iconPackManager: IconPackManager,
    private val settingsRepository: SettingsRepository
) {
    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()
    val mainUserApps: StateFlow<List<InstalledApp>> = installedApps
        .map { apps ->
            apps.filter { it.user == Process.myUserHandle() }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val workProfileApps: StateFlow<List<InstalledApp>> = installedApps
        .map { apps ->
            apps.filter { it.user != Process.myUserHandle() }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val reloadMutex = Mutex()

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageAdded(packageName: String, user: UserHandle) = updateSingleApp(packageName, user)
        override fun onPackageRemoved(packageName: String, user: UserHandle) = removeSingleApp(packageName, user)
        override fun onPackageChanged(packageName: String, user: UserHandle) = updateSingleApp(packageName, user)
        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
            packageNames.forEach { updateSingleApp(it, user) }
        }
        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
            packageNames.forEach { removeSingleApp(it, user) }
        }
    }

    init {
        launcherApps.registerCallback(callback)

        // Listen for icon pack changes
        scope.launch {
            settingsRepository.iconPackPackage.collect { pack ->
                iconPackManager.setIconPack(pack)
                performReload()
            }
        }
    }

    fun reloadApps() {
        scope.launch { performReload() }
    }

    private fun removeSingleApp(packageName: String, user: UserHandle) {
        _installedApps.update { current ->
            current.filterNot { it.packageName == packageName && it.user == user }
        }
    }

    private fun updateSingleApp(packageName: String, user: UserHandle) {
        scope.launch(Dispatchers.IO) {
            try {
                // If it's our own package, do not add it
                if (packageName == context.packageName) return@launch
                
                val activities = launcherApps.getActivityList(packageName, user)
                if (activities.isEmpty()) return@launch // Not a launchable app

                val info = activities.first()
                val cached = modifiedAppsRepository.getByPackageId(packageName)

                // 1. Get Install Time
                val installTime = cached?.installTime ?: try {
                    context.packageManager.getPackageInfo(packageName, 0).firstInstallTime
                } catch (e: Exception) {
                    0L
                }

                // 2. Get Theme Color
                val themeColor = cached?.themeColor ?: iconColorCache.getOrExtractColor(packageName)

                // 3. Update Cache if needed
                if (cached?.themeColor == null || cached.installTime == null) {
                    modifiedAppsRepository.setMetadata(packageName, themeColor, installTime)
                }

                val newApp = InstalledApp(
                    displayName = info.label.toString(),
                    packageName = packageName,
                    componentName = info.componentName,
                    user = user,
                    themeColor = themeColor,
                    installTime = installTime
                )

                _installedApps.update { current ->
                    (current.filterNot { it.packageName == packageName && it.user == user } + newApp)
                        .sortedBy { it.displayName.lowercase() }
                }
            } catch (e: Exception) {
                AppLogger.e("AppsRepository: Error updating app $packageName for user $user", e)
            }
        }
    }

    private fun performReload() {
        scope.launch(Dispatchers.IO) {
            reloadMutex.withLock {
                val allApps = mutableListOf<InstalledApp>()
                
                // Fetch all cached metadata at once
                val cachedMetadata = modifiedAppsRepository.getAllModifiedApps()
                    .associateBy { it.packageId }

                supervisorScope {
                    val extractionDispatcher = Dispatchers.IO.limitedParallelism(4)
                    userManager.userProfiles.forEach { userHandle ->
                        try {
                            val activities = launcherApps.getActivityList(null, userHandle)
                            val deferredApps = activities.map { info ->
                                async(extractionDispatcher) {
                                    try {
                                        val packageName = info.applicationInfo.packageName
                                        if (packageName != context.packageName) {
                                            val cached = cachedMetadata[packageName]
                                            
                                            // 1. Get Install Time (cached or fresh)
                                            val installTime = cached?.installTime ?: try {
                                                context.packageManager.getPackageInfo(packageName, 0).firstInstallTime
                                            } catch (e: Exception) {
                                                0L
                                            }

                                            // 2. Get Theme Color (cached or fresh)
                                            // We only re-extract if the cached value is null
                                            val themeColor = cached?.themeColor ?: iconColorCache.getOrExtractColor(packageName)

                                            // 3. Update Cache if we got fresh data
                                            if (cached?.themeColor == null || cached.installTime == null) {
                                                modifiedAppsRepository.setMetadata(packageName, themeColor, installTime)
                                            }

                                            InstalledApp(
                                                displayName = info.label.toString(),
                                                packageName = packageName,
                                                componentName = info.componentName,
                                                user = userHandle,
                                                themeColor = themeColor,
                                                installTime = installTime
                                            )
                                        } else null
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }
                            allApps.addAll(deferredApps.awaitAll().filterNotNull())
                        } catch (e: Exception) {
                            AppLogger.e("AppsRepository: Error reloading apps for user $userHandle", e)
                        }
                    }
                }

                _installedApps.value = allApps
                    .distinctBy { it.packageName + it.user.toString() }
                    .sortedBy { it.displayName.lowercase() }
            }
        }
    }

    /**
     * Returns the app name from its package
     *
     * @param packageName Name of the package that's app name will be returned
     * @return String app name
     */
    fun getAppNameFromPackageName(packageName: String): String {
        // Check current installed apps first
        _installedApps.value.find { it.packageName == packageName }?.let {
            return it.displayName
        }
        return "null"
    }

    /**
     * Returns an InstalledApp object from a package name
     *
     * @param packageName Name of the package
     * @return InstalledApp? or null if not found
     */
    fun getInstalledAppFromPackageName(packageName: String): InstalledApp? {
        // Check current installed apps first
        _installedApps.value.find { it.packageName == packageName }?.let {
            return it
        }
        return null
    }
}