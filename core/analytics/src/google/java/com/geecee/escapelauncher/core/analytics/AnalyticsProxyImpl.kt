package com.geecee.escapelauncher.core.analytics

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
class AnalyticsProxyImpl : AnalyticsProxy {
    private val crashlytics by lazy {
        try {
            Firebase.crashlytics
        } catch (e: Exception) {
            null
        }
    }

    override fun configureAnalytics(context: Context, enabled: Boolean) {
        try {
            val analytics = Firebase.analytics
            analytics.setConsent(
                mapOf(
                    FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (enabled) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED,
                )
            )
            analytics.setAnalyticsCollectionEnabled(enabled)
        } catch (e: Exception) {
            // Ignore if Firebase Analytics fails to initialize
        }
    }

    override fun logCustomKey(key: String, value: String) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun recordException(exception: Exception) {
        try {
            crashlytics?.recordException(exception)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
