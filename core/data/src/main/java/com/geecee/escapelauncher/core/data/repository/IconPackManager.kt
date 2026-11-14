package com.geecee.escapelauncher.core.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ComponentName string (e.g. "ComponentInfo{com.pkg/com.pkg.Activity}") -> Drawable name (e.g. "ic_custom_app")
    private val iconMap = mutableMapOf<String, String>()
    var currentIconPackPackage: String? = null
        private set
        
    private val _iconPackReady = MutableStateFlow(0)
    val iconPackReady: StateFlow<Int> = _iconPackReady.asStateFlow()

    suspend fun setIconPack(packageName: String?) {
        withContext(Dispatchers.IO) {
            currentIconPackPackage = packageName
            iconMap.clear()

            if (packageName != null) {
                try {
                    val pm = context.packageManager
                    val res = pm.getResourcesForApplication(packageName)
                    val appFilterId = res.getIdentifier("appfilter", "xml", packageName)

                    if (appFilterId > 0) {
                        val parser = res.getXml(appFilterId)
                        var eventType = parser.eventType
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                                val component = parser.getAttributeValue(null, "component")
                                val drawable = parser.getAttributeValue(null, "drawable")
                                if (component != null && drawable != null) {
                                    iconMap[component] = drawable
                                }
                            }
                            eventType = parser.next()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _iconPackReady.value += 1
        }
    }

    fun getIconForPackage(packageName: String, defaultDrawable: Drawable?): Drawable? {
        val iconPack = currentIconPackPackage ?: return defaultDrawable
        try {
            // Check if we have an explicit mapping for this package in appfilter.xml
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            val componentName = launchIntent?.component?.flattenToString()

            val mappedDrawableName = componentName?.let { "ComponentInfo{$it}" }?.let { iconMap[it] }
                ?: iconMap.entries.firstOrNull { it.key.contains(packageName) }?.value

            if (mappedDrawableName != null) {
                val res = pm.getResourcesForApplication(iconPack)
                val resId = res.getIdentifier(mappedDrawableName, "drawable", iconPack)
                if (resId > 0) {
                    return res.getDrawable(resId, null)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        return defaultDrawable
    }
}
