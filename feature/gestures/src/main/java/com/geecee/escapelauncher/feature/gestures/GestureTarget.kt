package com.geecee.escapelauncher.feature.gestures

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import com.geecee.escapelauncher.core.data.repository.DirectCallManager

sealed interface GestureTarget {
    val entryKey: String

    data class App(val packageName: String) : GestureTarget {
        override val entryKey: String = packageName
    }

    data class ContactCall(
        val phoneNumber: String,
        val displayName: String
    ) : GestureTarget {
        override val entryKey: String = "call:${phoneNumber.trim()}:${displayName.trim()}"
    }

    fun title(pm: PackageManager): String = when (this) {
        is App -> try {
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName
        }
        is ContactCall -> displayName.ifBlank { phoneNumber }
    }

    fun subtitle(pm: PackageManager): String = when (this) {
        is App -> "Opens ${title(pm)}"
        is ContactCall -> "Calls ${displayName.ifBlank { phoneNumber }} ($phoneNumber)"
    }

    fun execute(context: Context) {
        when (this) {
            is App -> {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
                }
            }
            is ContactCall -> {
                DirectCallManager.placeCallImmediately(
                    context = context,
                    phoneNumber = phoneNumber,
                    contactName = displayName
                )
            }
        }
    }

    companion object {
        fun parse(entryKey: String): GestureTarget {
            if (entryKey.startsWith("call:")) {
                val payload = entryKey.removePrefix("call:")
                val sepIdx = payload.indexOf(':')
                return if (sepIdx >= 0) {
                    val number = payload.substring(0, sepIdx)
                    val name = payload.substring(sepIdx + 1)
                    ContactCall(phoneNumber = number, displayName = name.ifBlank { number })
                } else {
                    ContactCall(phoneNumber = payload, displayName = payload)
                }
            }
            val pkg = entryKey.removePrefix("app:")
            return App(packageName = pkg)
        }
    }
}
