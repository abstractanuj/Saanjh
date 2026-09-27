package com.geecee.escapelauncher.core.common

import timber.log.Timber

/**
 * Enhanced logger for the application.
 * Wraps Timber to provide a consistent interface and potentially persist logs to a file in the future.
 */
object AppLogger {

    // region Initialization
    fun init() {
        if (Timber.treeCount == 0 && BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
    // endregion

    // region Logging Methods

    fun v(message: String, t: Throwable? = null, tag: String? = null, vararg args: Any) {
        tag?.let { Timber.tag(it) }
        if (t != null) {
            if (args.isEmpty()) Timber.v(t, message) else Timber.v(t, message, *args)
        } else {
            if (args.isEmpty()) Timber.v(message) else Timber.v(message, *args)
        }
    }

    fun d(message: String, t: Throwable? = null, tag: String? = null, vararg args: Any) {
        tag?.let { Timber.tag(it) }
        if (t != null) {
            if (args.isEmpty()) Timber.d(t, message) else Timber.d(t, message, *args)
        } else {
            if (args.isEmpty()) Timber.d(message) else Timber.d(message, *args)
        }
    }

    fun i(message: String, t: Throwable? = null, tag: String? = null, vararg args: Any) {
        tag?.let { Timber.tag(it) }
        if (t != null) {
            if (args.isEmpty()) Timber.i(t, message) else Timber.i(t, message, *args)
        } else {
            if (args.isEmpty()) Timber.i(message) else Timber.i(message, *args)
        }
    }

    fun w(message: String, t: Throwable? = null, tag: String? = null, vararg args: Any) {
        tag?.let { Timber.tag(it) }
        if (t != null) {
            if (args.isEmpty()) Timber.w(t, message) else Timber.w(t, message, *args)
        } else {
            if (args.isEmpty()) Timber.w(message) else Timber.w(message, *args)
        }
    }

    fun e(message: String, t: Throwable? = null, tag: String? = null, vararg args: Any) {
        tag?.let { Timber.tag(it) }
        if (t != null) {
            if (args.isEmpty()) Timber.e(t, message) else Timber.e(t, message, *args)
        } else {
            if (args.isEmpty()) Timber.e(message) else Timber.e(message, *args)
        }
    }

    // endregion
}
