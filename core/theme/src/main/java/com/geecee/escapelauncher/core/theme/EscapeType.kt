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
            letterSpacing = (-2.0).sp
        ), headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            lineHeight = 64.sp,
            letterSpacing = (-1.5).sp
        ), headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 56.sp,
            lineHeight = 56.sp,
            letterSpacing = (-1.0).sp
        ), titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            letterSpacing = (-0.5).sp
        ), titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 32.sp,
            lineHeight = 48.sp,
            letterSpacing = 0.sp
        ), titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 26.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.5.sp
        ), bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 20.sp,
            lineHeight = 34.sp,
            letterSpacing = 0.5.sp
        ), bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 18.sp,
            lineHeight = 30.sp,
            letterSpacing = 0.4.sp
        ), bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Light,
            fontSize = 15.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.3.sp
        )
    )

    return typography
}