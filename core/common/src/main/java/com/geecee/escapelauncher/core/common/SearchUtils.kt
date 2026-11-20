package com.geecee.escapelauncher.core.common

import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.normalizeSearchText

/**
 * High-speed O(N) Smith-Waterman / Fzy-inspired fuzzy affinity engine.
 * Eliminates per-call Regex compilation and string splitting.
 */
fun fuzzyMatch(text: String, pattern: String): Boolean {
    if (pattern.isEmpty()) return true
    if (text.isEmpty()) return false
    val normText = normalizeSearchText(text)
    val normPattern = normalizeSearchText(pattern)
    return computeAffinityScore(normText, "", normPattern) >= 0
}

/**
 * Scores an [InstalledApp] against a pre-normalized [normalizedQuery].
 * Returns -1 if not a match, or a positive affinity score (higher = stronger match).
 */
fun scoreInstalledApp(app: InstalledApp, normalizedQuery: String): Int {
    if (normalizedQuery.isEmpty()) return 0
    return computeAffinityScore(app.normalizedName, app.initials, normalizedQuery)
}

fun computeAffinityScore(
    normText: String,
    initials: String,
    normPattern: String
): Int {
    if (normPattern.isEmpty()) return 0
    val tLen = normText.length
    val pLen = normPattern.length
    if (tLen == 0) return -1

    // 1. Exact full match
    if (normText == normPattern) return 10_000

    // 2. Exact prefix match (shorter names rank higher)
    if (normText.startsWith(normPattern)) {
        return 6_000 - tLen.coerceAtMost(100)
    }

    // 3. Initials / Acronym match (e.g., "gm" -> "Google Maps")
    if (pLen >= 2) {
        val resolvedInitials = if (initials.isNotEmpty()) initials else buildInitials(normText)
        if (resolvedInitials.length >= pLen) {
            if (resolvedInitials.startsWith(normPattern)) {
                return 4_500 - tLen.coerceAtMost(100)
            }
            if (resolvedInitials.contains(normPattern)) {
                return 3_800 - tLen.coerceAtMost(100)
            }
        }
    }

    // 4. Contiguous substring match
    val subIndex = normText.indexOf(normPattern)
    if (subIndex >= 0) {
        val isWordBoundary = subIndex == 0 || !normText[subIndex - 1].isLetterOrDigit()
        val boundaryBonus = if (isWordBoundary) 1_200 else 0
        return 2_500 + boundaryBonus - (subIndex * 18) - tLen.coerceAtMost(100)
    }

    // 5. Subsequence alignment with consecutive-run bonus and gap penalty
    if (pLen > tLen) return -1
    var tIdx = 0
    var pIdx = 0
    var score = 1_000
    var consecutiveRun = 0
    var lastMatchIdx = -1

    while (tIdx < tLen && pIdx < pLen) {
        if (normText[tIdx] == normPattern[pIdx]) {
            val isWordStart = tIdx == 0 || !normText[tIdx - 1].isLetterOrDigit()
            if (isWordStart) {
                score += 65
            }
            if (lastMatchIdx == tIdx - 1) {
                consecutiveRun++
                score += consecutiveRun * 35
            } else {
                if (lastMatchIdx >= 0) {
                    val gap = tIdx - lastMatchIdx - 1
                    score -= (gap * 8).coerceAtMost(120)
                }
                consecutiveRun = 1
            }
            lastMatchIdx = tIdx
            pIdx++
        }
        tIdx++
    }

    return if (pIdx == pLen) score.coerceAtLeast(1) else -1
}

private fun buildInitials(normText: String): String {
    val sb = StringBuilder(6)
    var takeNext = true
    for (i in 0 until normText.length) {
        val c = normText[i]
        if (c == ' ' || c == '-' || c == '_') {
            takeNext = true
        } else if (takeNext) {
            sb.append(c)
            takeNext = false
        }
    }
    return sb.toString()
}
