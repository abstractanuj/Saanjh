package com.geecee.escapelauncher.feature.widgets.people

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.appwidget.AppWidgetManager
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object AtomCloudRenderer {

    /**
     * Extracts the current dynamic widget dimensions in dp from AppWidgetManager options.
     */
    fun resolveWidgetSizeDp(
        options: Bundle?,
        defaultWidthDp: Float = 240f,
        defaultHeightDp: Float = 150f
    ): Pair<Float, Float> {
        if (options == null) return defaultWidthDp to defaultHeightDp
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minW)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minH)

        val wDp = if (minW > 0 && maxW > 0) (minW + maxW) * 0.5f else defaultWidthDp
        val hDp = if (minH > 0 && maxH > 0) (minH + maxH) * 0.5f else defaultHeightDp
        return wDp.coerceAtLeast(72f) to hDp.coerceAtLeast(56f)
    }

    /**
     * Generates an adaptive, size-aware Atom Cloud bitmap tailored to the widget's exact current
     * width and height:
     * - Computes a dedicated safety margin around all 4 edges of the widget.
     * - Uses a blended superelliptical + elliptical harmonic distance field with 3-octave FBM noise
     *   and twinkling night stars so the cloud generously fills the interior of the widget up to
     *   the safety margin while mathematically guaranteeing (alpha == 0) that the cloud NEVER crosses
     *   the margin border or touches the widget edges.
     */
    fun createCommonAtomCloudBitmap(
        widgetWidthDp: Float = 240f,
        widgetHeightDp: Float = 150f
    ): Bitmap {
        // Render at ~1.65 px per dp, capped so Binder IPC stays well under limits
        val scale = 1.65f
        val rawW = (widgetWidthDp * scale).toInt().coerceIn(180, 680)
        val rawH = (widgetHeightDp * scale).toInt().coerceIn(130, 680)

        // Dedicated safety margin in pixels around all 4 borders that the cloud NEVER crosses
        val marginDp = (min(widgetWidthDp, widgetHeightDp) * 0.085f).coerceIn(10f, 18f)
        val marginX = (marginDp * (rawW / widgetWidthDp.coerceAtLeast(1f))).coerceAtLeast(12f)
        val marginY = (marginDp * (rawH / widgetHeightDp.coerceAtLeast(1f))).coerceAtLeast(12f)

        val cx = rawW * 0.5f
        val cy = rawH * 0.5f
        // Safe half-extents inside the margin border
        val safeHalfW = max(24f, cx - marginX)
        val safeHalfH = max(24f, cy - marginY)

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val isNight = hour >= 19 || hour <= 4

        // Time-of-day Atom Cloud palette matching Khoj Search
        val (c1, c2, c3) = when (hour) {
            in 5..10 -> Triple(
                floatArrayOf(0.75f, 0.09f, 0.36f, 0.96f), // Deep Rose Berry core
                floatArrayOf(0.58f, 0.20f, 0.92f, 0.90f), // Vibrant Orchid Violet mid
                floatArrayOf(0.42f, 0.13f, 0.66f, 0.80f)  // Rich Plum outer halo
            )
            in 11..15 -> Triple(
                floatArrayOf(0.01f, 0.41f, 0.63f, 0.96f), // Deep Ocean Cobalt core
                floatArrayOf(0.01f, 0.52f, 0.78f, 0.90f), // Vibrant Azure Cyan mid
                floatArrayOf(0.12f, 0.25f, 0.69f, 0.80f)  // Rich Sapphire outer halo
            )
            in 16..18 -> Triple(
                floatArrayOf(0.76f, 0.25f, 0.05f, 0.96f), // Deep Burnt Amber-Crimson core
                floatArrayOf(0.92f, 0.35f, 0.05f, 0.90f), // Vibrant Sunset Tangerine mid
                floatArrayOf(0.49f, 0.18f, 0.07f, 0.80f)  // Rich Dusk Terracotta outer halo
            )
            else -> Triple(
                floatArrayOf(0.06f, 0.09f, 0.17f, 0.97f), // Deep Cosmic Obsidian core
                floatArrayOf(0.12f, 0.23f, 0.54f, 0.91f), // Rich Midnight Indigo mid
                floatArrayOf(0.11f, 0.31f, 0.85f, 0.82f)  // Deep Royal Blue outer halo
            )
        }

        val phase = (hour * 1.7f) + 2.3f
        val cosT1 = cos(phase * 0.4f)
        val sinT1 = sin(phase * 0.4f)
        val cosT2 = cos(phase * 0.27f)
        val sinT2 = sin(phase * 0.27f)

        val pixels = IntArray(rawW * rawH)
        val minSafe = min(safeHalfW, safeHalfH)

        for (y in 0 until rawH) {
            val dy = y - cy
            val ny = dy / safeHalfH
            // Hard guarantee: outside top/bottom margin, pixel is 100% transparent
            if (ny <= -1f || ny >= 1f) continue

            val ny2 = ny * ny
            val ny4 = ny2 * ny2
            val rowOffset = y * rawW

            for (x in 0 until rawW) {
                val dx = x - cx
                val nx = dx / safeHalfW
                // Hard guarantee: outside left/right margin, pixel is 100% transparent
                if (nx <= -1f || nx >= 1f) continue

                val nx2 = nx * nx
                val nx4 = nx2 * nx2

                // Blend elliptical distance with superelliptical (squircle) distance so the cloud
                // expands to fill rectangular widgets generously while staying strictly < 1 at the margin
                val dEllipse = sqrt(nx2 + ny2)
                val dSuper = (nx4 + ny4).pow(0.25f)
                val baseD = dEllipse * 0.52f + dSuper * 0.48f

                if (baseD >= 1.05f) continue

                val invD = 1f / max(dEllipse, 0.0001f)
                val ux = nx * invD
                val uy = ny * invD

                // Chebyshev harmonic lobes matching Khoj Search CLOUD_SHADER
                val cos2a = ux * ux - uy * uy
                val sin2a = 2f * ux * uy
                val cos3a = ux * (4f * ux * ux - 3f)
                val sin3a = uy * (3f * -4f * uy * uy + 3f)

                val wave2 = sin2a * cosT1 + cos2a * sinT1
                val wave3 = cos3a * cosT2 + sin3a * sinT2
                val n = fbm(nx * 1.6f + 1.3f, ny * 1.6f + 2.1f)

                // Scale wobble down smoothly near the margin border (baseD -> 1.0) so it never crosses 1.0
                val borderDamping = (1f - baseD).coerceIn(0f, 1f)
                val wobble = (wave2 * 0.045f + wave3 * 0.035f + (n - 0.5f) * 0.11f) * borderDamping
                val d = (baseD + wobble).coerceAtLeast(0f)

                if (d >= 0.98f) continue

                // Generous interior plateau + smooth fade out before the margin border (d == 0.98)
                val edgeAlpha = 1f - smoothstep(0.64f, 0.98f, d)
                if (edgeAlpha <= 0.002f) continue

                val core = exp(-d * 1.45f)
                val halo = exp(-d * 0.58f) * 0.88f

                var r = mix(c3[0], c2[0], halo)
                var g = mix(c3[1], c2[1], halo)
                var b = mix(c3[2], c2[2], halo)
                var a = mix(c3[3], c2[3], halo)

                r = mix(r, c1[0], core)
                g = mix(g, c1[1], core)
                b = mix(b, c1[2], core)
                a = mix(a, c1[3], core)

                // Twinkling stars inside the cloud
                val starCellX = floor(nx * minSafe * 0.14f)
                val starCellY = floor(ny * minSafe * 0.14f)
                val starHash = hash2D(starCellX.toInt(), starCellY.toInt())
                if (starHash > 0.982f && d < 0.82f) {
                    val starBoost = (if (isNight) 0.55f else 0.28f) * ((starHash - 0.982f) / 0.018f)
                    r = (r + starBoost).coerceAtMost(1f)
                    g = (g + starBoost).coerceAtMost(1f)
                    b = (b + starBoost).coerceAtMost(1f)
                }

                val finalA = (a * edgeAlpha).coerceIn(0f, 1f)
                val aInt = (finalA * 255f).toInt()
                if (aInt <= 0) continue

                val rInt = (r * 255f).toInt().coerceIn(0, 255)
                val gInt = (g * 255f).toInt().coerceIn(0, 255)
                val bInt = (b * 255f).toInt().coerceIn(0, 255)

                pixels[rowOffset + x] = Color.argb(aInt, rInt, gInt, bInt)
            }
        }

        val bitmap = Bitmap.createBitmap(rawW, rawH, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, rawW, 0, 0, rawW, rawH)
        return bitmap
    }

    private fun mix(a: Float, b: Float, t: Float): Float = a + (b - a) * t.coerceIn(0f, 1f)

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun hash2D(ix: Int, iy: Int): Float {
        var n = ix * 374761393 + iy * 668265263
        n = (n xor (n ushr 13)) * 1274126177
        n = n xor (n ushr 16)
        return (n and 0x7fffffff).toFloat() / 2147483647f
    }

    private fun valueNoise(x: Float, y: Float): Float {
        val ix = floor(x).toInt()
        val iy = floor(y).toInt()
        val fx = x - ix
        val fy = y - iy
        val ux = fx * fx * (3f - 2f * fx)
        val uy = fy * fy * (3f - 2f * fy)

        val a = hash2D(ix, iy)
        val b = hash2D(ix + 1, iy)
        val c = hash2D(ix, iy + 1)
        val d = hash2D(ix + 1, iy + 1)

        return mix(mix(a, b, ux), mix(c, d, ux), uy)
    }

    private fun fbm(px: Float, py: Float): Float {
        var v = 0f
        var amp = 0.5f
        var x = px
        var y = py
        for (i in 0 until 3) {
            v += amp * valueNoise(x, y)
            x *= 2f
            y *= 2f
            amp *= 0.5f
        }
        return v
    }
}
