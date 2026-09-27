package com.geecee.escapelauncher.feature.homescreen.todayspicks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geecee.escapelauncher.feature.homescreen.TodaysPicksViewModel

@Composable
fun TodaysPicksScreen(
    modifier: Modifier = Modifier,
    viewModel: TodaysPicksViewModel = hiltViewModel()
) {
    val todaysPicks by viewModel.todaysPicks.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp)
    ) {
        // Header
        Text(
            text = "Today's Picks",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        when {
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            error != null -> {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Failed to load picks\n${error?.message}",
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            todaysPicks != null -> {
                val picks = todaysPicks!!
                // Content state
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    // Video cards
                    items(picks.videos, key = { it.id }) { video ->
                        VideoCard(
                            video = video,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Article cards
                    items(picks.articles, key = { it.id }) { article ->
                        ArticleCard(
                            article = article,
                            onToggleSummary = { articleId ->
                                viewModel.toggleArticleSummary(articleId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Empty state
                    if (picks.articles.isEmpty() && picks.videos.isEmpty()) {
                        item {
                            Text(
                                text = "No picks available today",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
            
            else -> {
                // Initial state (no data yet)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
