package com.geecee.escapelauncher.feature.gestures

import android.Manifest
import android.content.pm.PackageManager
import android.gesture.GestureLibraries
import android.gesture.GestureLibrary
import android.gesture.GestureOverlayView
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF6F3EC)) {
                    GestureSetupNavGraph(gestureLibrary)
                }
            }
        }
    }
}

enum class GestureScreen {
    List, SelectApp, SelectContact, Draw
}

@Composable
fun GestureSetupNavGraph(gestureLibrary: GestureLibrary) {
    var currentScreen by remember { mutableStateOf(GestureScreen.List) }
    var selectedTargetKey by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    when (currentScreen) {
        GestureScreen.List -> {
            GestureListScreen(
                gestureLibrary = gestureLibrary,
                refreshTrigger = refreshTrigger,
                onBindAppClick = { currentScreen = GestureScreen.SelectApp },
                onBindContactClick = { currentScreen = GestureScreen.SelectContact },
                onDelete = { entryName ->
                    gestureLibrary.removeEntry(entryName)
                    gestureLibrary.save()
                    refreshTrigger++
                }
            )
        }
        GestureScreen.SelectApp -> {
            BackHandler { currentScreen = GestureScreen.List }
            AppSelectionScreen(
                onAppSelected = { packageName ->
                    selectedTargetKey = GestureTarget.App(packageName).entryKey
                    currentScreen = GestureScreen.Draw
                },
                onBack = { currentScreen = GestureScreen.List }
            )
        }
        GestureScreen.SelectContact -> {
            BackHandler { currentScreen = GestureScreen.List }
            ContactSelectionScreen(
                onContactSelected = { name, number ->
                    selectedTargetKey = GestureTarget.ContactCall(
                        phoneNumber = number,
                        displayName = name
                    ).entryKey
                    currentScreen = GestureScreen.Draw
                },
                onBack = { currentScreen = GestureScreen.List }
            )
        }
        GestureScreen.Draw -> {
            BackHandler { currentScreen = GestureScreen.List }
            DrawGestureScreen(
                gestureLibrary = gestureLibrary,
                targetEntryKey = selectedTargetKey!!,
                onComplete = {
                    currentScreen = GestureScreen.List
                    refreshTrigger++
                },
                onBack = { currentScreen = GestureScreen.List }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureListScreen(
    gestureLibrary: GestureLibrary,
    refreshTrigger: Int,
    onBindAppClick: () -> Unit,
    onBindContactClick: () -> Unit,
    onDelete: (String) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val savedEntries = remember(refreshTrigger) {
        gestureLibrary.gestureEntries?.toList() ?: emptyList()
    }

    Scaffold(
        containerColor = Color(0xFFF6F3EC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "saanjh · Gestures",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181614)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F3EC))
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ExtendedFloatingActionButton(
                    onClick = onBindContactClick,
                    containerColor = Color(0xFF2D2A26),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Bind Contact Call")
                    Spacer(Modifier.width(8.dp))
                    Text("Call Contact")
                }
                ExtendedFloatingActionButton(
                    onClick = onBindAppClick,
                    containerColor = Color(0xFF181614),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Bind App Gesture")
                    Spacer(Modifier.width(8.dp))
                    Text("Bind App")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Text(
                "BOUND GESTURES (${savedEntries.size}) · SINGLE-STROKE INSTANT / MULTI-STROKE 1.8S",
                color = Color(0xFF756E64),
                fontSize = 11.sp,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(savedEntries) { entryName ->
                    val target = GestureTarget.parse(entryName)
                    val gestures = gestureLibrary.getGestures(entryName)
                    val maxStrokes = gestures?.maxOfOrNull { it.strokesCount } ?: 1
                    val strokeModeLabel = if (maxStrokes > 1) {
                        "Multi-stroke ($maxStrokes strokes)"
                    } else {
                        "Single-stroke (instant)"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFBF7)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    target.title(pm),
                                    color = Color(0xFF181614),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${target.subtitle(pm)} · $strokeModeLabel",
                                    color = Color(0xFF4A453E),
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = { onDelete(entryName) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete gesture",
                                    tint = Color(0xFF8C3B2B)
                                )
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
            .sortedBy { packageManager.getApplicationLabel(it).toString().lowercase() }
    }

    Scaffold(
        containerColor = Color(0xFFF6F3EC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Target App",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181614)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF181614)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F3EC))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(installedApps, key = { it.packageName }) { appInfo ->
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = {
                        Text(
                            appName,
                            color = Color(0xFF181614),
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    supportingContent = {
                        Text(appInfo.packageName, color = Color(0xFF756E64), fontSize = 12.sp)
                    },
                    modifier = Modifier.clickable { onAppSelected(appInfo.packageName) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSelectionScreen(
    onContactSelected: (name: String, number: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasPermissions = result[Manifest.permission.READ_CONTACTS] == true ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE
                )
            )
        }
    }

    val contacts = remember(hasPermissions) {
        if (!hasPermissions) {
            emptyList()
        } else {
            val list = ArrayList<Pair<String, String>>()
            val seen = HashSet<String>()
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            try {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                )?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameIdx)?.trim() ?: continue
                        val num = cursor.getString(numIdx)?.trim() ?: continue
                        if (name.isNotEmpty() && num.isNotEmpty() && seen.add("$name|$num")) {
                            list.add(name to num)
                        }
                    }
                }
            } catch (_: Exception) {
            }
            list
        }
    }

    Scaffold(
        containerColor = Color(0xFFF6F3EC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Contact to Call",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181614)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF181614)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F3EC))
            )
        }
    ) { padding ->
        if (!hasPermissions) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Contacts and Phone permissions are required to bind direct-call gestures.",
                    color = Color(0xFF181614),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        permLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.CALL_PHONE
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF181614))
                ) {
                    Text("Grant Contacts & Call Permission")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(contacts, key = { "${it.first}_${it.second}" }) { (name, number) ->
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = {
                            Text(
                                name,
                                color = Color(0xFF181614),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text(number, color = Color(0xFF756E64), fontSize = 12.sp)
                        },
                        modifier = Modifier.clickable { onContactSelected(name, number) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawGestureScreen(
    gestureLibrary: GestureLibrary,
    targetEntryKey: String,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var variationCount by remember { mutableIntStateOf(1) }
    var multiStrokeMode by remember { mutableStateOf(false) }
    val maxVariations = 5

    Scaffold(
        containerColor = Color(0xFFF6F3EC),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Record Gesture ($variationCount / $maxVariations)",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF181614)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF181614)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F3EC))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = !multiStrokeMode,
                    onClick = { multiStrokeMode = false },
                    label = { Text("Single-Stroke (Instant)") }
                )
                FilterChip(
                    selected = multiStrokeMode,
                    onClick = { multiStrokeMode = true },
                    label = { Text("Multi-Stroke (Lift Hand)") }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF141B29))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        GestureOverlayView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            isEventsInterceptionEnabled = true
                            gestureStrokeWidth = 22f
                            gestureColor = AndroidColor.WHITE
                            uncertainGestureColor = AndroidColor.parseColor("#B0FFFFFF")
                            gestureStrokeType = if (multiStrokeMode) {
                                GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE
                            } else {
                                GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE
                            }
                            fadeOffset = if (multiStrokeMode) 1800L else 420L

                            addOnGesturePerformedListener { _, gesture ->
                                gestureLibrary.addGesture(targetEntryKey, gesture)
                                gestureLibrary.save()

                                if (variationCount < maxVariations) {
                                    Toast.makeText(
                                        ctx,
                                        "Sample $variationCount recorded (${gesture.strokesCount} stroke${if (gesture.strokesCount > 1) "s" else ""})! Trace again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    variationCount++
                                    clear(false)
                                } else {
                                    Toast.makeText(
                                        ctx,
                                        "All $maxVariations samples saved!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onComplete()
                                }
                            }
                        }
                    },
                    update = { view ->
                        view.gestureStrokeType = if (multiStrokeMode) {
                            GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE
                        } else {
                            GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE
                        }
                        view.fadeOffset = if (multiStrokeMode) 1800L else 420L
                    }
                )
            }

            Text(
                if (multiStrokeMode) {
                    "Multi-Stroke mode: lift your hand to draw multiple strokes; waits 1.8s after your last stroke to save."
                } else {
                    "Single-Stroke mode: executes immediately on finger lift."
                },
                color = Color(0xFF4A453E),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onComplete) {
                Text(
                    "Finish Recording Early",
                    color = Color(0xFF8C3B2B),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}