package com.geecee.escapelauncher.core.model

import android.content.ComponentName
import android.os.Process
import android.os.UserHandle
import java.text.Normalizer
import java.util.regex.Pattern

private val DIACRITICS_PATTERN: Pattern = Pattern.compile("\\p{M}+")

fun normalizeSearchText(input: String): String {
    if (input.isEmpty()) return ""
    var isAscii = true
    for (i in 0 until input.length) {
        if (input[i].code >= 128) {
            isAscii = false
            break
        }
    }
    val base = if (isAscii) input else DIACRITICS_PATTERN.matcher(
        Normalizer.normalize(input, Normalizer.Form.NFD)
    ).replaceAll("")
    return base.lowercase()
}

private fun extractInitials(rawName: String, normalizedName: String): String {
    val sb = StringBuilder(8)
    var prevWasSeparator = true
    for (i in 0 until rawName.length) {
        val c = rawName[i]
        if (c == ' ' || c == '-' || c == '_' || c == '.') {
            prevWasSeparator = true
        } else if (prevWasSeparator || (i > 0 && c.isUpperCase() && rawName[i - 1].isLowerCase())) {
            sb.append(c.lowercaseChar())
            prevWasSeparator = false
        }
    }
    if (sb.length <= 1 && normalizedName.isNotEmpty()) {
        return normalizedName.take(1)
    }
    return sb.toString()
}

/**
 * Data class representing an installed application with pre-indexed search tokens
 * for O(N) zero-allocation fuzzy affinity ranking.
 */
data class InstalledApp(
    val displayName: String,
    val packageName: String,
    val componentName: ComponentName,
    val user: UserHandle = Process.myUserHandle(),
    val themeColor: Int? = null,
    val installTime: Long = 0L
) {
    val normalizedName: String = normalizeSearchText(displayName)
    val initials: String = extractInitials(displayName, normalizedName)
}