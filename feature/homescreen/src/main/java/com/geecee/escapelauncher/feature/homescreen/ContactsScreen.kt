package com.geecee.escapelauncher.feature.homescreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.provider.CallLog
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.geecee.escapelauncher.core.ui.composables.*
import com.geecee.escapelauncher.core.theme.primaryContentColor
import androidx.compose.ui.graphics.Brush
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val peopleTiles by viewModel.peopleTiles.collectAsStateWithLifecycle()
    val backgroundUri by viewModel.peoplePageBackgroundUri.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val now = remember { System.currentTimeMillis() }
    val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000L)

    val backgroundBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F0F0F),
                Color(0xFF1A1A1A),
                Color(0xFF0F0F0F)
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (backgroundUri != null) {
            AsyncImage(
                model = backgroundUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        } else {
            Box(modifier = Modifier.fillMaxSize().background(backgroundBrush))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // HEADERS ONLY AT TOP (AS THEY WERE)
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "PEOPLE",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = if (backgroundUri != null) Color.White else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "they'd love to hear from you".uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (backgroundUri != null) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // REFINED BENTO GROUP ENGINE (ADAPTIVE DENSITY)
            BentoGroup(
                items = peopleTiles,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                orientation = BentoOrientation.Vertical,
                pageSize = 10, // Increased to 10 to fill more vertical space
                itemContent = { tile, bentoSize ->
                    val bentoModifier = Modifier.fillMaxSize()
                    when (tile) {
                        is PeopleTile.Contact -> {
                            val contact = tile.contact
                            val call = tile.lastCall
                            
                            val isTooOld = call != null && call.timestamp < sevenDaysAgo
                            
                            ContactTile(
                                name = contact.name,
                                onClick = { viewModel.callContact(context, contact.phoneNumber) },
                                tileSize = when(bentoSize) {
                                    BentoItemSize.Large -> TileSize.Large
                                    BentoItemSize.Medium -> TileSize.Medium
                                    BentoItemSize.Tall -> TileSize.Tall
                                    else -> TileSize.Small
                                },
                                tileColor = Color(contact.tileColor),
                                modifier = bentoModifier,
                                timestamp = call?.timestamp,
                                callType = if (isTooOld) null else call?.type,
                                isReturned = call?.isReturned ?: false,
                                hasBackgroundImage = backgroundUri != null
                            )
                        }
                        is PeopleTile.Missed -> {
                            val call = tile.call
                            ContactTile(
                                name = call.name ?: call.number,
                                onClick = { viewModel.callContact(context, call.number) },
                                tileSize = when(bentoSize) {
                                    BentoItemSize.Large -> TileSize.Large
                                    BentoItemSize.Medium -> TileSize.Medium
                                    BentoItemSize.Tall -> TileSize.Tall
                                    else -> TileSize.Small
                                },
                                tileColor = if (call.isReturned) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = bentoModifier,
                                timestamp = call.timestamp,
                                callType = CallLog.Calls.MISSED_TYPE,
                                isReturned = call.isReturned,
                                hasBackgroundImage = backgroundUri != null
                            )
                        }
                    }
                }
            )

            // NO DUPLICATE HEADERS AT BOTTOM
            Spacer(Modifier.height(32.dp))
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ContactTile(
    name: String,
    onClick: () -> Unit,
    tileSize: TileSize,
    tileColor: Color,
    modifier: Modifier = Modifier,
    timestamp: Long? = null,
    callType: Int? = null,
    isReturned: Boolean = false,
    hasBackgroundImage: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tileScale"
    )

    val timeString = remember(timestamp) {
        timestamp?.let { ts ->
            val now = System.currentTimeMillis()
            val diff = now - ts
            when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
            }
        }
    }

    val nudgeText = when {
        isReturned -> "Returned"
        callType == CallLog.Calls.MISSED_TYPE -> "Missed"
        callType == CallLog.Calls.INCOMING_TYPE -> "Received"
        callType == CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
        else -> null
    }

    val icon = when {
        isReturned -> Icons.Default.Check
        callType == CallLog.Calls.MISSED_TYPE -> Icons.AutoMirrored.Filled.CallMissed
        callType == CallLog.Calls.INCOMING_TYPE -> Icons.AutoMirrored.Filled.CallReceived
        callType == CallLog.Calls.OUTGOING_TYPE -> Icons.AutoMirrored.Filled.CallMade
        else -> null
    }

    val containerColor = if (hasBackgroundImage) {
        Color.Black.copy(alpha = 0.3f)
    } else {
        tileColor.copy(alpha = 0.8f)
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // Custom scale is our indication
                onClick = {}, // Requirements state double click to call
                onDoubleClick = onClick,
                onLongClick = null
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (hasBackgroundImage) BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopEnd).size(14.dp),
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            Column {
                if (nudgeText != null) {
                    Text(
                        text = nudgeText.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (tileSize == TileSize.Large) 16.sp else 14.sp
                    ),
                    color = Color.White,
                    maxLines = 2
                )
                
                if (timeString != null) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
