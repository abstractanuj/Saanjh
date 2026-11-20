package com.geecee.escapelauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.geecee.escapelauncher.core.data.repository.DirectCallManager

class DirectCallHelperActivity : ComponentActivity() {

    private var pendingPhone: String = ""

    private val requestCallPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (pendingPhone.isNotEmpty()) {
            val action = if (granted) Intent.ACTION_CALL else Intent.ACTION_DIAL
            try {
                startActivity(
                    Intent(action).apply {
                        data = Uri.parse("tel:${Uri.encode(pendingPhone)}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } catch (_: Exception) {
            }
        }
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        pendingPhone = intent?.getStringExtra(DirectCallManager.EXTRA_PHONE_NUMBER)?.trim().orEmpty()
        if (pendingPhone.isEmpty()) {
            finish()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                startActivity(
                    Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:${Uri.encode(pendingPhone)}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } catch (_: Exception) {
            }
            finish()
            overridePendingTransition(0, 0)
        } else {
            requestCallPermission.launch(Manifest.permission.CALL_PHONE)
        }
    }
}
