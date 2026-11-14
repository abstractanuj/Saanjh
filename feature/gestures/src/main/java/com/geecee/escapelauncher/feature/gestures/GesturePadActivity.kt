package com.geecee.escapelauncher.feature.gestures

import android.app.Activity
import android.content.Intent
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.GestureOverlayView
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.io.File

class GesturePadActivity : Activity() {

    private lateinit var gestureLibrary: GestureLibrary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fluid enter animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 48
        }

        val storeFile = File(filesDir, "gestures")
        gestureLibrary = GestureLibraries.fromFile(storeFile)
        gestureLibrary.load()

        val rootLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#55002233")) // Frosted ocean tint
        }

        val instructionsLayout = LinearLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val iconView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                bottomMargin = 24
            }
            setImageResource(R.drawable.ic_scribble_gesture)
            setColorFilter(Color.WHITE)
        }

        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = "Draw gesture to launch app"
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            alpha = 0.7f
        }

        instructionsLayout.addView(iconView)
        instructionsLayout.addView(textView)
        rootLayout.addView(instructionsLayout)

        val gestureOverlayView = GestureOverlayView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isEventsInterceptionEnabled = true
            
            // Initial style
            gestureStrokeWidth = 24f
            gestureColor = Color.WHITE
            uncertainGestureColor = Color.parseColor("#88FFFFFF")

            addOnGesturePerformedListener { _, gesture ->
                val predictions = gestureLibrary.recognize(gesture)
                if (!predictions.isNullOrEmpty()) {
                    val bestMatch = predictions.maxByOrNull { it.score }
                    if (bestMatch != null && bestMatch.score > 2.0) {
                        val packageName = bestMatch.name
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        } else {
                            Toast.makeText(this@GesturePadActivity, "App not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@GesturePadActivity, "Gesture not recognized", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@GesturePadActivity, "Gesture not recognized", Toast.LENGTH_SHORT).show()
                }
                finish()
                // Fluid exit animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
        
        rootLayout.addView(gestureOverlayView)
        setContentView(rootLayout)
    }
}
