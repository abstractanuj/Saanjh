package com.geecee.escapelauncher.feature.gestures

import android.content.Intent
import android.content.pm.PackageManager
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.GestureOverlayView
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.MotionEvent
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

class GestureSetupActivity : ComponentActivity() {

    private lateinit var gestureLibrary: GestureLibrary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val storeFile = File(filesDir, "gestures")
        gestureLibrary = GestureLibraries.fromFile(storeFile)
        gestureLibrary.load()
        
        setContent {
            MaterialTheme {
                // Glyf PRO inspired dark theme
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1E1E24)) {
                    GestureSetupNavGraph(gestureLibrary)
                }
            }
        }
    }
}

enum class GestureScreen {
    List, SelectApp, Draw
}

@Composable
fun GestureSetupNavGraph(gestureLibrary: GestureLibrary) {
    var currentScreen by remember { mutableStateOf(GestureScreen.List) }
    var selectedAppPackage by remember { mutableStateOf<String?>(null) }
    
    // Force recomposition when returning to list
    var refreshTrigger by remember { mutableStateOf(0) }

    when (currentScreen) {
        GestureScreen.List -> {
            GestureListScreen(
                gestureLibrary = gestureLibrary,
                refreshTrigger = refreshTrigger,
                onAddClick = { currentScreen = GestureScreen.SelectApp },
                onDelete = { entryName ->
                    gestureLibrary.removeEntry(entryName)
                    gestureLibrary.save()
                    refreshTrigger++
                }
            )
        }
        GestureScreen.SelectApp -> {
            AppSelectionScreen(
                onAppSelected = { packageName ->
                    selectedAppPackage = packageName
                    currentScreen = GestureScreen.Draw
                },
                onBack = { currentScreen = GestureScreen.List }
            )
        }
        GestureScreen.Draw -> {
            DrawGestureScreen(
                gestureLibrary = gestureLibrary,
                targetPackage = selectedAppPackage!!,
                onComplete = {
                    currentScreen = GestureScreen.List
                    refreshTrigger++
                },
                onBack = { currentScreen = GestureScreen.SelectApp }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureListScreen(
    gestureLibrary: GestureLibrary,
    refreshTrigger: Int,
    onAddClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    
    // Read saved gestures
    val savedEntries = remember(refreshTrigger) {
        gestureLibrary.gestureEntries?.toList() ?: emptyList()
    }

    Scaffold(
        containerColor = Color(0xFF1E1E24),
        topBar = {
            TopAppBar(
                title = { Text("Glyf PRO Gestures", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E24))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(Modifier.width(8.dp))
                Text("Add App")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            
            // Overlay Permission Check
            if (!Settings.canDrawOverlays(context)) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336).copy(alpha = 0.2f)),
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        "Overlay permission required to draw gestures anywhere. Tap to grant.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Text(
                "GESTURES (${savedEntries.size} saved)",
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(savedEntries) { entryName ->
                    val appName = try {
                        val appInfo = pm.getApplicationInfo(entryName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: PackageManager.NameNotFoundException) {
                        "Unknown App"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C34)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(appName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Launches $appName", color = Color.Gray, fontSize = 14.sp)
                            }
                            IconButton(onClick = { onDelete(entryName) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(onAppSelected: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    
    val installedApps = remember {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .sortedBy { packageManager.getApplicationLabel(it).toString() }
    }

    Scaffold(
        containerColor = Color(0xFF1E1E24),
        topBar = {
            TopAppBar(
                title = { Text("Select Target App", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E24))
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(installedApps) { appInfo ->
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = { Text(appName, color = Color.White) },
                    supportingContent = { Text(appInfo.packageName, color = Color.Gray) },
                    modifier = Modifier.clickable { onAppSelected(appInfo.packageName) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawGestureScreen(
    gestureLibrary: GestureLibrary,
    targetPackage: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var variationCount by remember { mutableStateOf(1) }
    val maxVariations = 5 // Allow 5 variations for better predictability

    Scaffold(
        containerColor = Color(0xFF1E1E24),
        topBar = {
            TopAppBar(
                title = { Text("Draw Gesture ($variationCount/$maxVariations)", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E24))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2C2C34))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        // Custom GestureOverlayView to support rainbow stroke
                        object : GestureOverlayView(ctx) {
                            init {
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                isEventsInterceptionEnabled = true
                                gestureStrokeWidth = 24f // Thicker stroke
                                
                                // We use a custom shader in onDraw, but set a base color here
                                gestureColor = AndroidColor.WHITE
                            }

                            override fun onDraw(canvas: android.graphics.Canvas) {
                                // Access the internal paint object via reflection to set the shader
                                try {
                                    val paintField = GestureOverlayView::class.java.getDeclaredField("mGesturePaint")
                                    paintField.isAccessible = true
                                    val paint = paintField.get(this) as Paint
                                    
                                    val width = width.toFloat()
                                    val height = height.toFloat()
                                    
                                    // Rainbow / Space Gradient based on "Glyf" theme requests
                                    val shader = LinearGradient(
                                        0f, 0f, width, height,
                                        intArrayOf(
                                            AndroidColor.parseColor("#FF0000"), // Red
                                            AndroidColor.parseColor("#FF7F00"), // Orange
                                            AndroidColor.parseColor("#FFFF00"), // Yellow
                                            AndroidColor.parseColor("#00FF00"), // Green
                                            AndroidColor.parseColor("#0000FF"), // Blue
                                            AndroidColor.parseColor("#4B0082"), // Indigo
                                            AndroidColor.parseColor("#8B00FF")  // Violet
                                        ),
                                        null,
                                        Shader.TileMode.CLAMP
                                    )
                                    paint.shader = shader
                                    paint.setShadowLayer(10f, 0f, 0f, AndroidColor.WHITE) // Neon glow
                                } catch (e: Exception) {
                                    // Fallback if reflection fails
                                }
                                super.onDraw(canvas)
                            }
                        }.apply {
                            addOnGesturePerformedListener { _, gesture ->
                                gestureLibrary.addGesture(targetPackage, gesture)
                                gestureLibrary.save()
                                
                                if (variationCount < maxVariations) {
                                    Toast.makeText(ctx, "Variation $variationCount saved! Draw again.", Toast.LENGTH_SHORT).show()
                                    variationCount++
                                    // Clear the gesture to allow the next one immediately
                                    clear(false)
                                } else {
                                    Toast.makeText(ctx, "All $maxVariations gestures saved successfully!", Toast.LENGTH_SHORT).show()
                                    onComplete()
                                }
                            }
                        }
                    }
                )
            }
            Text(
                "Draw the gesture exactly as you normally would. \nWe need $maxVariations variations for better predictability.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Allow early completion
            TextButton(onClick = onComplete) {
                Text("Finish Early (Not Recommended)")
            }
        }
    }
}