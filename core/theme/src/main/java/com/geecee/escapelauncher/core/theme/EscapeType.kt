package com.geecee.escapelauncher.core.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

fun escapeType(fontFamily: FontFamily): Typography {
    val typography = Typography(
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 72.sp,
            lineHeight = 72.sp,
            letterSpacing = (-1.5).sp
        ), headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            lineHeight = 64.sp,
            letterSpacing = (-1.0).sp
        ), headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 56.sp,
            lineHeight = 56.sp,
            letterSpacing = (-0.5).sp
        ), titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ), titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.15.sp
        ), titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.1.sp
        ), bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.5.sp
        ), bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.25.sp
        ), bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.4.sp
        )
    )

    return typography
}