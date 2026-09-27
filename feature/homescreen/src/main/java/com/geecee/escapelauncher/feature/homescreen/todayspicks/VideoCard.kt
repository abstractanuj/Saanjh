package com.geecee.escapelauncher.feature.homescreen.todayspicks

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.model.TodaysPickVideo

@Composable
fun VideoCard(
    video: TodaysPickVideo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Keep it clean for the picks list
                onClick = { /* No-op to allow long press to shine */ },
                onLongClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeLink))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                }
            )
            .padding(vertical = 16.dp)
    ) {
        // Channel label
        Text(
            text = video.channel.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Color(0xFFD32F2F),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Video title
        Text(
            text = video.title,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 40.sp,
                letterSpacing = (-0.5).sp,
                lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                    alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                    trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.None
                )
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Duration (if available)
        if (!video.duration.isNullOrEmpty()) {
            Text(
                text = "DURATION: ${video.duration}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
