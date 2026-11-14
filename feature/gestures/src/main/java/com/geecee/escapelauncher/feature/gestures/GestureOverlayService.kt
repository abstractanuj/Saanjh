package com.geecee.escapelauncher.feature.gestures

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

class GestureOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayButton: Button

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Modern floating button (Glyf PRO inspired)
        overlayButton = Button(this).apply {
            text = "〰" // Swiggly line to represent gesture
            textSize = 24f
            setBackgroundColor(Color.parseColor("#4400BCD4")) // Translucent cyan
            setTextColor(Color.WHITE)
            elevation = 8f
            setOnClickListener {
                val intent = Intent(this@GestureOverlayService, GesturePadActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            }
        }

        val layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val params = WindowManager.LayoutParams(
            150, // Width
            150, // Height
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.END // Docked to the right edge
            x = 0
            y = 0
        }

        try {
            windowManager.addView(overlayButton, params)
        } catch (e: Exception) {
            Toast.makeText(this, "Permission denied for overlay", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayButton.isInitialized) {
            windowManager.removeView(overlayButton)
        }
    }
}
