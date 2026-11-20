package com.geecee.escapelauncher.core.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat

object DirectCallManager {
    const val ACTION_DIRECT_CALL = "com.sareprne.saanjh.action.DIRECT_CALL"
    const val EXTRA_PHONE_NUMBER = "extra_phone_number"
    const val EXTRA_CONTACT_NAME = "extra_contact_name"

    fun placeCallImmediately(
        context: Context,
        phoneNumber: String,
        contactName: String? = null
    ) {
        val cleaned = phoneNumber.trim()
        if (cleaned.isEmpty()) {
            Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
            return
        }

        // Record in-app call timestamp for the People widget
        val prefs = context.getSharedPreferences(PeopleRepository.PREFS_NAME, Context.MODE_PRIVATE)
        val norm = PeopleRepository.normalizeNumber(cleaned)
        if (norm.isNotEmpty()) {
            prefs.edit().putLong("last_call_$norm", System.currentTimeMillis()).apply()
            PeopleRepository.notifyWidgetChanged(context)
        }

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCallPermission) {
            try {
                val callIntent = Intent(Intent.ACTION_CALL).apply {
                    data = Uri.parse("tel:${Uri.encode(cleaned)}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(callIntent)
                return
            } catch (_: Exception) {
            }
        }

        // Launch lightweight permission-requesting helper activity so CALL_PHONE can be granted on the spot
        try {
            val helperIntent = Intent(ACTION_DIRECT_CALL).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_PHONE_NUMBER, cleaned)
                putExtra(EXTRA_CONTACT_NAME, contactName ?: cleaned)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(helperIntent)
        } catch (_: Exception) {
            // Fallback to dialer if helper cannot be started
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${Uri.encode(cleaned)}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(dialIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Unable to place call", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
