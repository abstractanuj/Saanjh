package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.theme.CardContainerColor
import com.geecee.escapelauncher.core.theme.ContentColor
import kotlinx.coroutines.delay
import java.util.*

enum class HomeView {
    Home, Todo
}

@Composable
fun VerticalNavBar(
    selectedView: HomeView,
    onViewChange: (HomeView) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(CardContainerColor.copy(alpha = 0.3f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NavIcon(Icons.Default.Home, selectedView == HomeView.Home) { onViewChange(HomeView.Home) }
        NavIcon(Icons.Default.CheckCircle, selectedView == HomeView.Todo) { onViewChange(HomeView.Todo) }
    }
}

@Composable
private fun NavIcon(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) ContentColor.copy(alpha = 0.15f) else Color.Transparent)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isSelected) ContentColor else ContentColor.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoMiniApp() {
    // Moved to feature module to avoid circular dependency
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Tasks implementation moved", color = ContentColor)
    }
}

@Composable
fun PomodoroMiniApp() {
    // Pomodoro logic removed as requested
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Focus Mode Removed", color = ContentColor)
    }
}
