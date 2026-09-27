package com.geecee.escapelauncher.core.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import com.geecee.escapelauncher.core.model.InstalledApp

/**
 * Extracts the primary color from an app icon using Palette API.
 */
fun extractColorFromIcon(context: Context, packageName: String): Int? {
    return try {
        val icon = context.packageManager.getApplicationIcon(packageName)
        val bitmap = drawableToBitmap(icon)
        val palette = Palette.from(bitmap).generate()
        
        // Try to get a vibrant or dominant color
        val color = palette.getVibrantColor(0)
            .takeIf { it != 0 }
            ?: palette.getLightVibrantColor(0)
            .takeIf { it != 0 }
            ?: palette.getDominantColor(0)
            .takeIf { it != 0 }
            
        if (!bitmap.isRecycled && icon !is BitmapDrawable) {
            bitmap.recycle()
        }
        
        color
    } catch (e: Exception) {
        null
    }
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }

    // Limit the size to avoid massive memory allocations for large vectors or adaptive icons
    val targetWidth = drawable.intrinsicWidth.coerceIn(1, 128)
    val targetHeight = drawable.intrinsicHeight.coerceIn(1, 128)

    val bitmap = Bitmap.createBitmap(
        targetWidth,
        targetHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}