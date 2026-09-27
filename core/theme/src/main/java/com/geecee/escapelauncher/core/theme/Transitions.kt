package com.geecee.escapelauncher.core.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.graphics.TransformOrigin

// Smooth spring for entering elements - Slightly more damped for a "weighted" feel
private val SmoothSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessLow
)

// Emphasized easing curves for M3-like fluidity
// Decelerate for elements entering the screen - Fast start, very smooth finish
private val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

// Accelerate for elements leaving the screen - Slow start, snappy finish
private val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

// Standard duration for high-end feel
const val TRANSITION_DURATION = 400 

fun enterTransition() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { (it * 0.08f).toInt() } // Subtler parallax
) + scaleIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialScale = 0.96f, 
    transformOrigin = TransformOrigin(0.5f, 0.5f)
) + fadeIn(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedDecelerateEasing)
)

fun exitTransition() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { -(it * 0.08f).toInt() }
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedAccelerateEasing)
) + scaleOut(
    targetScale = 0.96f,
    animationSpec = tween(durationMillis = TRANSITION_DURATION, easing = EmphasizedAccelerateEasing)
)

fun popEnterTransition() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { -(it * 0.08f).toInt() }
) + scaleIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialScale = 0.96f
) + fadeIn(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedDecelerateEasing)
)

fun popExitTransition() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { (it * 0.08f).toInt() }
) + scaleOut(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetScale = 0.96f,
    transformOrigin = TransformOrigin(0.5f, 0.5f)
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedAccelerateEasing)
)
