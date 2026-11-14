package com.geecee.escapelauncher.core.ui.composables

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Universal Glassmorphic overlay for Android 12+ with safe fallback.
 */
fun Modifier.glassmorphicOverlay(
    color: Color = Color.Black.copy(alpha = 0.4f),
    blurRadius: Dp = 48.dp
): Modifier = this.then(
    Modifier
        .background(color)
        .then(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.blur(blurRadius)
            } else {
                Modifier
            }
        )
)

/**
 * A modifier for tactile elements that morphs their shape and pushes down slightly when pressed.
 */
fun Modifier.bouncyMorphable(
    restRadius: Dp = 32.dp,
    pressedRadius: Dp = 16.dp,
    pressedScale: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) pressedRadius else restRadius,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "corner_radius"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )

    this
        .scale(scale)
        .clip(RoundedCornerShape(cornerRadius))
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
