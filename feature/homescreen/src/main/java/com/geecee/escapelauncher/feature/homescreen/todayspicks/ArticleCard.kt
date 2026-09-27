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
import com.geecee.escapelauncher.core.model.TodaysPickArticle

@Composable
fun ArticleCard(
    article: TodaysPickArticle,
    onToggleSummary: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onToggleSummary(article.id) },
                onLongClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                }
            )
            .padding(vertical = 16.dp)
    ) {
        // Source label
        Text(
            text = article.source.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Headline
        Text(
            text = article.headline,
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
            maxLines = if (article.isSummaryExpanded) 10 else 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // Summary
        if (article.summary.isNotBlank()) {
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = if (article.isSummaryExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
