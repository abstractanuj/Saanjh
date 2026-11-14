package com.geecee.escapelauncher.feature.widgets

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.geecee.escapelauncher.core.theme.EscapeTheme
import com.geecee.escapelauncher.feature.widgets.ui.KhojSearchScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KhojSearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 48
        }
        
        setContent {
            EscapeTheme {
                KhojSearchScreen(
                    onDismiss = { finish() }
                )
            }
        }
    }
}
