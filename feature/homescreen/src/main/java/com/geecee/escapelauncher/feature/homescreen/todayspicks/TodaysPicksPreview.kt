package com.geecee.escapelauncher.feature.homescreen.todayspicks

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.geecee.escapelauncher.core.model.TodaysPickArticle
import com.geecee.escapelauncher.core.model.TodaysPickVideo

@Preview(showBackground = true, widthDp = 320, heightDp = 120)
@Composable
fun ArticleCardPreview() {
    MaterialTheme {
        Surface {
            ArticleCard(
                article = TodaysPickArticle(
                    id = "1",
                    headline = "How to become the person you want to be",
                    summary = "Close the intention-action gap and live the way you want. This article explores practical strategies...",
                    link = "https://aeon.co/guides/how-to-become-the-person-you-want-to-be",
                    source = "Aeon",
                    publishedAt = System.currentTimeMillis(),
                    isSummaryExpanded = false
                ),
                onToggleSummary = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 100)
@Composable
fun VideoCardPreview() {
    MaterialTheme {
        Surface {
            VideoCard(
                video = TodaysPickVideo(
                    id = "abc123",
                    title = "How to manage anxiety in uncertain times",
                    youtubeLink = "https://youtube.com/watch?v=abc123",
                    channel = "TED Talks",
                    publishedAt = System.currentTimeMillis(),
                    duration = "15:30"
                )
            )
        }
    }
}
