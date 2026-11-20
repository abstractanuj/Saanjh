package com.geecee.escapelauncher.feature.gestures

import android.gesture.Gesture
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.GesturePoint
import android.gesture.GestureStroke
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.hypot

private data class TimedInkPoint(
    val x: Float,
    val y: Float,
    val timestamp: Long,
    val strokeWidth: Float
)

class GesturePadActivity : ComponentActivity() {

    private lateinit var gestureLibrary: GestureLibrary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 48
        }

        val storeFile = File(filesDir, "gestures")
        gestureLibrary = GestureLibraries.fromFile(storeFile)
        gestureLibrary.load()

        setContent {
            MinimalGesturePad(
                shouldExecuteSingleStrokeImmediately = { firstStroke ->
                    tryInstantSingleStrokeDispatch(firstStroke)
                },
                onMultiStrokeGestureCompleted = { strokes ->
                    recognizeAndExecuteMultiStroke(strokes)
                },
                onDismiss = {
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            )
        }
    }

    /**
     * Checks whether a single completed stroke should execute immediately (0 ms delay):
     * - If the stroke has >= 4 points and matches a saved 1-stroke gesture with high confidence,
     *   AND either there are no multi-stroke gestures in the library OR the 1-stroke gesture is the
     *   unambiguous top match, it executes immediately and returns true.
     * - If multi-stroke gestures exist in the library and could be continued by lifting the hand
     *   and adding more strokes, returns false so the 1.8s multi-stroke window waits for additional strokes.
     */
    private fun tryInstantSingleStrokeDispatch(strokePoints: List<TimedInkPoint>): Boolean {
        if (strokePoints.size < 4) return false

        val entries = gestureLibrary.gestureEntries ?: return false
        var hasAnyMultiStrokeEntries = false
        val singleStrokeEntryNames = HashSet<String>()

        for (entry in entries) {
            val savedGestures = gestureLibrary.getGestures(entry) ?: continue
            for (g in savedGestures) {
                if (g.strokesCount > 1) {
                    hasAnyMultiStrokeEntries = true
                } else if (g.strokesCount == 1) {
                    singleStrokeEntryNames.add(entry)
                }
            }
        }

        val gesture = buildGestureFromStrokes(listOf(strokePoints))
        val predictions = gestureLibrary.recognize(gesture) ?: return false
        if (predictions.isEmpty()) return false

        val bestOverall = predictions.maxByOrNull { it.score } ?: return false
        val bestSingleStroke = predictions
            .filter { it.name in singleStrokeEntryNames }
            .maxByOrNull { it.score }

        // If the user has only single-stroke gestures saved, execute immediately on finger lift
        if (!hasAnyMultiStrokeEntries && bestSingleStroke != null && bestSingleStroke.score > 1.75) {
            GestureTarget.parse(bestSingleStroke.name).execute(this)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            return true
        }

        // If both single-stroke and multi-stroke gestures exist, execute immediately if the top
        // match is a single-stroke gesture with strong confidence
        if (bestSingleStroke != null &&
            bestOverall.name == bestSingleStroke.name &&
            bestSingleStroke.score >= 2.25
        ) {
            GestureTarget.parse(bestSingleStroke.name).execute(this)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            return true
        }

        return false
    }

    private fun recognizeAndExecuteMultiStroke(strokes: List<List<TimedInkPoint>>) {
        val validStrokes = strokes.filter { it.size >= 2 }
        val totalPoints = validStrokes.sumOf { it.size }
        if (validStrokes.isEmpty() || totalPoints < 4) {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            return
        }

        val gesture = buildGestureFromStrokes(validStrokes)
        val predictions = gestureLibrary.recognize(gesture)

        if (!predictions.isNullOrEmpty()) {
            val strokeCount = validStrokes.size
            // Prefer entries whose recorded stroke count matches the drawn stroke count when scores are close
            val matchingStrokeCountEntries = HashSet<String>()
            gestureLibrary.gestureEntries?.forEach { entry ->
                val hasMatchingCount = gestureLibrary.getGestures(entry)?.any {
                    it.strokesCount == strokeCount
                } == true
                if (hasMatchingCount) {
                    matchingStrokeCountEntries.add(entry)
                }
            }

            val bestMatchingCount = predictions
                .filter { it.name in matchingStrokeCountEntries && it.score > 1.55 }
                .maxByOrNull { it.score }

            val bestOverall = predictions.maxByOrNull { it.score }
            val chosen = bestMatchingCount ?: bestOverall?.takeIf { it.score > 1.7 }

            if (chosen != null) {
                GestureTarget.parse(chosen.name).execute(this)
            } else {
                Toast.makeText(this, "Gesture not recognized", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Gesture not recognized", Toast.LENGTH_SHORT).show()
        }

        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun buildGestureFromStrokes(strokes: List<List<TimedInkPoint>>): Gesture {
        return Gesture().apply {
            for (stroke in strokes) {
                if (stroke.size >= 2) {
                    val pts = ArrayList<GesturePoint>(stroke.size)
                    for (p in stroke) {
                        pts.add(GesturePoint(p.x, p.y, p.timestamp))
                    }
                    addStroke(GestureStroke(pts))
                }
            }
        }
    }

    companion object {
        private const val MULTI_STROKE_WAIT_MS = 1800L
    }
}

@Composable
private fun MinimalGesturePad(
    shouldExecuteSingleStrokeImmediately: (List<TimedInkPoint>) -> Boolean,
    onMultiStrokeGestureCompleted: (List<List<TimedInkPoint>>) -> Unit,
    onDismiss: () -> Unit
) {
    val completedStrokes = remember { mutableStateListOf<List<TimedInkPoint>>() }
    val currentStroke = remember { mutableStateListOf<TimedInkPoint>() }
    val scope = rememberCoroutineScope()
    val pendingFinishJob = remember { arrayOfNulls<Job>(1) }

    BackHandler(onBack = {
        pendingFinishJob[0]?.cancel()
        onDismiss()
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9050912))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("gesture_pad_canvas")
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        // Cancel the multi-stroke wait timer immediately when the user starts another stroke
                        pendingFinishJob[0]?.cancel()
                        currentStroke.clear()

                        val startTime = System.currentTimeMillis()
                        currentStroke.add(
                            TimedInkPoint(
                                x = down.position.x,
                                y = down.position.y,
                                timestamp = startTime,
                                strokeWidth = 20f
                            )
                        )

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            val pos = change.position
                            val now = System.currentTimeMillis()
                            val prev = currentStroke.lastOrNull()
                            if (prev != null) {
                                val dist = hypot(pos.x - prev.x, pos.y - prev.y)
                                if (dist >= 3f) {
                                    val dt = (now - prev.timestamp).coerceAtLeast(1L).toFloat()
                                    val velocity = dist / dt
                                    val targetWidth = (22f - velocity * 4.5f).coerceIn(10f, 24f)
                                    val smoothedWidth = prev.strokeWidth * 0.65f + targetWidth * 0.35f
                                    currentStroke.add(
                                        TimedInkPoint(
                                            x = pos.x,
                                            y = pos.y,
                                            timestamp = now,
                                            strokeWidth = smoothedWidth
                                        )
                                    )
                                }
                            }
                            change.consume()
                        } while (event.changes.any { it.pressed })

                        val finishedStroke = currentStroke.toList()
                        currentStroke.clear()
                        if (finishedStroke.size >= 2) {
                            completedStrokes.add(finishedStroke)
                        }

                        // If this was the first stroke and matches a single-stroke gesture unambiguously,
                        // execute immediately with 0 ms wait.
                        if (completedStrokes.size == 1 &&
                            shouldExecuteSingleStrokeImmediately(completedStrokes.first())
                        ) {
                            return@awaitEachGesture
                        }

                        // Otherwise wait 1.8 seconds after the last stroke so the user can lift their hand
                        // and draw additional strokes before matching.
                        pendingFinishJob[0] = scope.launch {
                            delay(1800L)
                            onMultiStrokeGestureCompleted(completedStrokes.toList())
                        }
                    }
                }
        ) {
            for (stroke in completedStrokes) {
                val count = stroke.size
                if (count >= 2) {
                    for (i in 0 until count - 1) {
                        val a = stroke[i]
                        val b = stroke[i + 1]
                        drawLine(
                            color = Color.White,
                            start = Offset(a.x, a.y),
                            end = Offset(b.x, b.y),
                            strokeWidth = b.strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            val currentCount = currentStroke.size
            if (currentCount >= 2) {
                for (i in 0 until currentCount - 1) {
                    val a = currentStroke[i]
                    val b = currentStroke[i + 1]
                    drawLine(
                        color = Color.White,
                        start = Offset(a.x, a.y),
                        end = Offset(b.x, b.y),
                        strokeWidth = b.strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
