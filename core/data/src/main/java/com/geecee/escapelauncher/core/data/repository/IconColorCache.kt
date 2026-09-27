package com.geecee.escapelauncher.core.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.palette.graphics.Palette
import com.geecee.escapelauncher.core.common.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconColorCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val colorCache = ConcurrentHashMap<String, Int>()

    fun getOrExtractColor(packageName: String): Int? {
        colorCache[packageName]?.let { return it }

        return try {
            val icon = context.packageManager.getApplicationIcon(packageName)
            val bitmap = drawableToBitmap(icon)
            val palette = Palette.from(bitmap).generate()
            
            val color: Int? = palette.getVibrantColor(0)
                .takeIf { it != 0 }
                ?: palette.getLightVibrantColor(0)
                .takeIf { it != 0 }
                ?: palette.getDominantColor(0)
                .takeIf { it != 0 }
            
            if (color != null) {
                colorCache[packageName] = color
            }
            // Free the bitmap to relieve memory pressure
            if (!bitmap.isRecycled && icon !is BitmapDrawable) {
                bitmap.recycle()
            }
            color
        } catch (e: Exception) {
            AppLogger.e("IconColorCache: Error extracting color for $packageName", e)
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

    fun clear() {
        colorCache.clear()
    }
}
