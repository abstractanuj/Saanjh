package com.geecee.escapelauncher

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

class AwarenessNudgeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            // Do NOT set FLAG_NOT_FOCUSABLE here so Android's WindowManager allows this window
            // to hide the status bar icons & text while the Dynamic Island is visible.
            @Suppress("DEPRECATION")
            addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            attributes = attributes.apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                y = 0
            }
        }

        hideStatusBarIcons()

        val prompt = pickNextPrompt()

        setContent {
            DynamicIslandNudgePill(
                prompt = prompt,
                onFinished = {
                    finish()
                    overridePendingTransition(0, 0)
                }
            )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideStatusBarIcons()
        }
    }

    private fun hideStatusBarIcons() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun pickNextPrompt(): String {
        val prefs = getSharedPreferences("saanjh_nudge_state", MODE_PRIVATE)
        val lastIdx = prefs.getInt("last_idx", -1)
        val nextIdx = (lastIdx + 1) % PROMPTS.size
        prefs.edit().putInt("last_idx", nextIdx).apply()
        return PROMPTS[nextIdx]
    }

    companion object {
        const val ACTION_AWARENESS_NUDGE = "com.sareprne.saanjh.action.AWARENESS_NUDGE"

        private val PROMPTS = listOf(
            "name 3 sounds you can hear right now",
            "look at the farthest thing you can see",
            "unclench your jaw and drop your shoulders",
            "notice the temperature of the air on your skin",
            "feel the weight of your feet on the ground",
            "take one slow breath all the way out",
            "soften your eyes and look away from the glass"
        )
    }
}

@Composable
private fun DynamicIslandNudgePill(
    prompt: String,
    onFinished: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        // Stay visible for 6 seconds then softly fade and contract away
        delay(6000L)
        visible = false
        delay(420L)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    visible = false
                    onFinished()
                }
            )
            .padding(top = 8.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(340, easing = FastOutSlowInEasing)) +
                expandHorizontally(
                    expandFrom = Alignment.CenterHorizontally,
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                ),
            exit = fadeOut(tween(360, easing = FastOutSlowInEasing)) +
                shrinkHorizontally(
                    shrinkTowards = Alignment.CenterHorizontally,
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                )
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 190.dp, max = 340.dp)
                    .testTag("awareness_nudge_pill"),
                color = Color(0xFF070709),
                shape = RoundedCornerShape(50),
                border = BorderStroke(0.75.dp, Color(0x26FFFFFF)),
                shadowElevation = 10.dp
            ) {
                Text(
                    text = prompt,
                    color = Color(0xFFECE8E1),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp)
                )
            }
        }
    }
}
