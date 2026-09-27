package com.geecee.escapelauncher.feature.homescreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.model.MediaState

@Composable
fun MediaCard(
    state: MediaState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = if (state.isPlaying) "NOW PLAYING" else "CONTINUE WHERE YOU LEFT OFF?",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = state.title ?: "Unknown Title",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Text(
            text = state.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
