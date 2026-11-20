package com.geecee.escapelauncher.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.geecee.escapelauncher.core.data.repository.DirectCallManager
import com.geecee.escapelauncher.core.data.repository.PeopleRepository
import com.geecee.escapelauncher.core.data.repository.PersonContact
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@AndroidEntryPoint
class FavoriteContactsActivity : ComponentActivity() {

    @Inject
    lateinit var peopleRepository: PeopleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0B0E)
                ) {
                    FavoriteContactsScreen(
                        peopleRepository = peopleRepository,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoriteContactsScreen(
    peopleRepository: PeopleRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var allDeviceContacts by remember { mutableStateOf<List<PersonContact>>(emptyList()) }

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
        refreshTrigger++
    }

    LaunchedEffect(Unit) {
        val needed = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE
        ).filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permLauncher.launch(needed.toTypedArray())
        }
    }

    LaunchedEffect(hasPermissions, refreshTrigger) {
        if (hasPermissions) {
            allDeviceContacts = peopleRepository.getAllDeviceContacts()
        }
    }

    val favoriteContacts = remember(refreshTrigger, hasPermissions) {
        peopleRepository.getFavoriteContactsSync()
    }
    val favoriteNormSet = remember(favoriteContacts) {
        favoriteContacts.map { PeopleRepository.normalizeNumber(it.phoneNumber) }.toSet()
    }

    val filteredDeviceContacts = remember(allDeviceContacts, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            allDeviceContacts
        } else {
            allDeviceContacts.filter {
                it.name.lowercase().contains(q) || it.phoneNumber.contains(q)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0A0B0E),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "people",
                        color = Color(0xFFECE8E1),
                        fontWeight = FontWeight.Light,
                        fontSize = 24.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFECE8E1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0B0E)
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!hasPermissions) {
                item {
                    Surface(
                        color = Color(0xFF17171A),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Allow Contacts, Call Log, and Phone access to see who you haven't talked to in 2+ weeks and double-tap their name to call.",
                                color = Color(0xFFD6D1C7),
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    permLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.READ_CONTACTS,
                                            Manifest.permission.READ_CALL_LOG,
                                            Manifest.permission.CALL_PHONE
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF282623),
                                    contentColor = Color(0xFFECE8E1)
                                )
                            ) {
                                Text("Grant Access")
                            }
                        }
                    }
                }
            }

            if (favoriteContacts.isNotEmpty()) {
                item {
                    Text(
                        "double-tap a name to call · shared atom cloud",
                        color = Color(0xFF7D7870),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    )
                }

                // Single common Atom Cloud background containing all favorite contacts
                item {
                    SharedAtomCloudContactsContainer(
                        contacts = favoriteContacts,
                        onDoubleTapContact = { person ->
                            DirectCallManager.placeCallImmediately(
                                context = context,
                                phoneNumber = person.phoneNumber,
                                contactName = person.name
                            )
                            refreshTrigger++
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "favorite people (${favoriteContacts.size})",
                    color = Color(0xFF9E978E),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Filter contacts", color = Color(0xFF6E6A64))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(
                filteredDeviceContacts,
                key = { "${it.id}_${it.phoneNumber}" }
            ) { contact ->
                val isFav = PeopleRepository.normalizeNumber(contact.phoneNumber) in favoriteNormSet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            peopleRepository.toggleFavoriteContact(contact)
                            refreshTrigger++
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            color = Color(0xFFECE8E1),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = contact.phoneNumber,
                            color = Color(0xFF7D7870),
                            fontSize = 12.sp
                        )
                    }
                    Checkbox(
                        checked = isFav,
                        onCheckedChange = {
                            peopleRepository.toggleFavoriteContact(contact)
                            refreshTrigger++
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SharedAtomCloudContactsContainer(
    contacts: List<PersonContact>,
    onDoubleTapContact: (PersonContact) -> Unit
) {
    val hour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val (coreColor, midColor, outerColor) = remember(hour) {
        when (hour) {
            in 5..10 -> Triple(
                Color(0xF2A81C58),
                Color(0xD97E2AB2),
                Color(0x91561A86)
            )
            in 11..15 -> Triple(
                Color(0xF20C629C),
                Color(0xD9107ABC),
                Color(0x911A3E94)
            )
            in 16..18 -> Triple(
                Color(0xF2B23E12),
                Color(0xD9CE5418),
                Color(0x91742A12)
            )
            else -> Triple(
                Color(0xF5101A34),
                Color(0xDE1E3A8A),
                Color(0x9B1C4CBC)
            )
        }
    }

    val textShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.7f),
            offset = Offset(0f, 2f),
            blurRadius = 6f
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Unified Common Atom Cloud Background sized to the container with a strict safety margin
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            val rng = Random(hour * 97 + 13)
            val cx = w * 0.5f
            val cy = h * 0.5f
            val marginX = w * 0.06f
            val marginY = h * 0.06f
            val safeHalfW = (w * 0.5f) - marginX
            val safeHalfH = (h * 0.5f) - marginY

            for (i in 0 until 6) {
                val angle = (i * (Math.PI * 2.0 / 6.0)) + rng.nextFloat() * 0.4f
                val lobeX = cx + (safeHalfW * 0.24f) * cos(angle).toFloat()
                val lobeY = cy + (safeHalfH * 0.24f) * sin(angle).toFloat()
                val radius = minOf(safeHalfW, safeHalfH) * (0.72f + rng.nextFloat() * 0.04f)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to coreColor,
                        0.46f to midColor,
                        0.76f to outerColor,
                        0.96f to Color.Transparent,
                        1.0f to Color.Transparent,
                        center = Offset(lobeX, lobeY),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(lobeX, lobeY)
                )
            }

            // Star specks across the inner Atom Cloud core
            for (s in 0 until 16) {
                val sx = cx + (rng.nextFloat() - 0.5f) * safeHalfW * 1.4f
                val sy = cy + (rng.nextFloat() - 0.5f) * safeHalfH * 1.4f
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f + rng.nextFloat() * 0.4f),
                    radius = 1.4f + rng.nextFloat() * 1.6f,
                    center = Offset(sx, sy)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = contacts.chunked(2)
            for (rowItems in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (person in rowItems) {
                        val seed = abs((person.name.hashCode() * 31) xor person.phoneNumber.hashCode())
                        val startPad = (4 + (seed % 14)).dp
                        val endPad = (4 + ((seed / 7) % 14)).dp
                        val topPad = (2 + ((seed / 13) % 8)).dp
                        val bottomPad = (2 + ((seed / 19) % 8)).dp

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    start = startPad,
                                    end = endPad,
                                    top = topPad,
                                    bottom = bottomPad
                                )
                                .testTag("people_pane_${person.phoneNumber}")
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {},
                                    onDoubleClick = { onDoubleTapContact(person) }
                                )
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = person.name,
                                color = if (person.isOverTwoWeeks) Color.White else Color(0xA6ECE8E1),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Light,
                                    shadow = textShadow,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 20.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = person.relativeLastTalkedLabel(),
                                color = if (person.isOverTwoWeeks) Color(0xE6ECE8E1) else Color(0x78ECE8E1),
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    shadow = textShadow,
                                    textAlign = TextAlign.Center
                                ),
                                maxLines = 1
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
