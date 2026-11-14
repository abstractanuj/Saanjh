package com.geecee.escapelauncher.core.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun EscapeTheme(
    theme: AppColourScheme? = null,
    fontName: String? = null,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable (() -> Unit)
) {
    val context = LocalContext.current
    val colorScheme by themeViewModel.theme.collectAsStateWithLifecycle(initialValue = AppColourScheme.OFF_LIGHT)
    val lScheme by themeViewModel.ltheme.collectAsStateWithLifecycle(initialValue = AppColourScheme.OFF_LIGHT)
    val dScheme by themeViewModel.dtheme.collectAsStateWithLifecycle(initialValue = AppColourScheme.DARK)
    val syncTheme by themeViewModel.syncTheme.collectAsStateWithLifecycle(initialValue = false)
    val font by themeViewModel.font.collectAsStateWithLifecycle(initialValue = "Jost")

    val fontFamily = remember(fontName, font) {
        getFontFamily(
            context = context,
            fontName = fontName ?: font
        )
    }

    val resolvedColorScheme =
        theme?.resolveColorScheme()
            ?: if (syncTheme) {
                if (isSystemInDarkTheme()) {
                    dScheme.resolveColorScheme()
                } else {
                    lScheme.resolveColorScheme()
                }
            } else {
                colorScheme.resolveColorScheme()
            }

    // Keep status bar icons readable against the themed background (dark icons on a light surface)
    val view = LocalView.current
    if (!view.isInEditMode) {
        val lightStatusBarIcons = resolvedColorScheme.surface.luminance() > 0.5f
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                lightStatusBarIcons
        }
    }

    MaterialTheme(
        colorScheme = resolvedColorScheme,
        typography = escapeType(fontFamily),
        content = content
    )
}

@Composable
fun EscapeThemePreview(content: @Composable (() -> Unit)) {
    MaterialTheme(
        colorScheme = offLightScheme,
        typography = escapeType(FontFamily.Default),
        content = content
    )
}

fun Modifier.glassmorphism(
    shape: Shape,
    color: Color = Color(0xCCFFFFFF), // More opaque stark white for less intensive "glass"
    borderColor: Color = Color(0x33FFFFFF),
    borderWidth: Dp = 1.dp
): Modifier = composed {
    this.clip(shape)
        .background(color)
        .border(borderWidth, borderColor, shape)
}
