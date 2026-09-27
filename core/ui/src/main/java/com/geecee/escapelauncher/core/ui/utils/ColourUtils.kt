package com.geecee.escapelauncher.core.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import android.graphics.Color as AndroidColor

/**
 * Convert a Compose Colour to an Android Colour.
 *
 * @return The Android Colour as an integer.
 */
fun Color.toAndroidColor(): Int {
    return AndroidColor.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}

/**
 * Centralized logic to derive soft background colors for app tiles/items.
 */
fun deriveSoftBackgroundColor(themeColor: Int?, isDark: Boolean): Color {
    val baseColor = themeColor?.let { Color(it) } ?: Color(0xFF607D8B)
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(baseColor.toArgb(), hsl)

    if (isDark) {
        // Significantly brighter and more saturated for dark theme
        hsl[1] = (hsl[1] * 0.7f).coerceIn(0.35f, 0.6f)
        hsl[2] = (hsl[2] * 0.4f + 0.35f).coerceIn(0.35f, 0.55f)
    } else {
        // More vibrant for light theme
        hsl[1] = (hsl[1] * 0.85f).coerceIn(0.4f, 0.6f)
        hsl[2] = (hsl[2] * 0.3f + 0.65f).coerceIn(0.8f, 0.95f)
    }
    return Color(ColorUtils.HSLToColor(hsl))
}
