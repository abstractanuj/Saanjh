package com.geecee.escapelauncher.core.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset

// Expressive spring for dynamic, organic entries
val ExpressiveSpringFloat = spring<Float>(
    dampingRatio = 0.7f,
    stiffness = Spring.StiffnessMediumLow
)

val ExpressiveSpringIntOffset = spring<IntOffset>(
    dampingRatio = 0.7f,
    stiffness = Spring.StiffnessMediumLow
)

// Snappy spring for quick, tactile exits
val SnappySpringFloat = spring<Float>(
    dampingRatio = 0.85f,
    stiffness = Spring.StiffnessMedium
)

val SnappySpringIntOffset = spring<IntOffset>(
    dampingRatio = 0.85f,
    stiffness = Spring.StiffnessMedium
)

fun enterTransition() = slideInHorizontally(
    animationSpec = ExpressiveSpringIntOffset,
    initialOffsetX = { (it * 0.1f).toInt() } // Deep parallax
) + scaleIn(
    animationSpec = ExpressiveSpringFloat,
    initialScale = 0.92f, 
    transformOrigin = TransformOrigin(0.5f, 0.5f)
) + fadeIn(
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
)

fun exitTransition() = slideOutHorizontally(
    animationSpec = SnappySpringIntOffset,
    targetOffsetX = { -(it * 0.05f).toInt() }
) + fadeOut(
    animationSpec = spring(stiffness = Spring.StiffnessMedium)
) + scaleOut(
    targetScale = 0.96f,
    animationSpec = SnappySpringFloat
)

fun popEnterTransition() = slideInHorizontally(
    animationSpec = ExpressiveSpringIntOffset,
    initialOffsetX = { -(it * 0.1f).toInt() }
) + scaleIn(
    animationSpec = ExpressiveSpringFloat,
    initialScale = 0.92f
) + fadeIn(
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
)

fun popExitTransition() = slideOutHorizontally(
    animationSpec = SnappySpringIntOffset,
    targetOffsetX = { (it * 0.05f).toInt() }
) + scaleOut(
    animationSpec = SnappySpringFloat,
    targetScale = 0.96f,
    transformOrigin = TransformOrigin(0.5f, 0.5f)
) + fadeOut(
    animationSpec = spring(stiffness = Spring.StiffnessMedium)
)
