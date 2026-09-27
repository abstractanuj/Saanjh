package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.geecee.escapelauncher.core.ui.utils.deriveSoftBackgroundColor
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Tile sizes for the bento/rigid grid
 */
enum class TileSize {
    Small,  // 1x1
    Medium, // 2x1 (Wide)
    Large,  // 2x2 (Big)
    Tall    // 1x2 (Vertical)
}

/**
 * A Metro-style grid item inspired by Windows Phone 8.
 * Features sharp corners, solid accent colors, and bottom-left labeling.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MasonryAppItem(
    modifier: Modifier = Modifier,
    appName: String,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    tileSize: TileSize = TileSize.Small,
    tileColor: Color? = null,
    imageUri: String? = null
) {
    val isDark = isSystemInDarkTheme()
    // M3 Soft Neutral Color Logic: Desaturate and manipulate the app's theme color based on theme
    val resolvedTileColor = remember(tileColor, isDark) {
        deriveSoftBackgroundColor(tileColor?.toArgb(), isDark)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Enhanced WP8-style "Tilt" and "Shrink" effect
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f, // Shrink slightly more on click
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 1500f),
        label = "scale"
    )
    
    val pressedOverlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.1f else 0f,
        animationSpec = spring(stiffness = 2000f),
        label = "overlay"
    )
    
    // Dynamically calculate content color for best contrast, while preferring white for dark tiles
    val contentColor = if (resolvedTileColor.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier = modifier
            .padding(2.dp) // Tight spacing for Metro grid
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shape = RoundedCornerShape(16.dp) // M3 Rounded Corners
                clip = true
            }
            .drawBehind {
                drawRect(resolvedTileColor)
                if (pressedOverlayAlpha > 0f) {
                    drawRect(Color.White.copy(alpha = pressedOverlayAlpha))
                }
            }
            .combinedClickable(
                onClick = onAppClick,
                onLongClick = onAppLongClick,
                interactionSource = interactionSource,
                indication = null
            )
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.8f
            )
        }

        // App name in the bottom-left corner with auto-resizing
        AutoResizingText(
            text = appName,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = if (tileSize == TileSize.Large) 15.sp else 13.sp,
                letterSpacing = 0.sp
            ),
            maxLines = 1,
            textAlign = TextAlign.Start,
            minFontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
    }
}
