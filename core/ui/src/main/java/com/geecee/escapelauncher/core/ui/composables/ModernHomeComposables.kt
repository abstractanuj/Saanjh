package com.geecee.escapelauncher.core.ui.composables

import android.content.ComponentName
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ModernTopBar(
    date: String,
    greeting: String = "",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = date.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        if (greeting.isNotBlank()) {
            Text(
                text = greeting.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickAppsBar(
    apps: List<InstalledApp>,
    onAppClick: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
    onAppLongClick: (InstalledApp) -> Unit = {}
) {
    BentoGroup(
        items = apps,
        modifier = modifier.fillMaxWidth(),
        orientation = BentoOrientation.Horizontal,
        height = 220.dp,
        pageSize = 3,
        itemContent = { app, bentoSize ->
            val color = app.themeColor?.let { Color(it) }
            MasonryAppItem(
                modifier = Modifier.fillMaxSize(),
                appName = app.displayName,
                onAppClick = { onAppClick(app) },
                onAppLongClick = { onAppLongClick(app) },
                tileSize = when (bentoSize) {
                    BentoItemSize.Large -> TileSize.Large
                    BentoItemSize.Medium -> TileSize.Medium
                    BentoItemSize.Tall -> TileSize.Tall
                    else -> TileSize.Small
                },
                tileColor = color
            )
        }
    )
}

@Preview
@Composable
fun ModernHomePreview() {
    val apps = listOf(
        InstalledApp("Calculator", "com.calc", ComponentName("com.calc", "Main"), themeColor = 0xFF00ABA9.toInt()),
        InstalledApp("Calendar", "com.cal", ComponentName("com.cal", "Main"), themeColor = 0xFF2D89EF.toInt()),
        InstalledApp("Camera", "com.cam", ComponentName("com.cam", "Main"), themeColor = 0xFF603CBA.toInt()),
        InstalledApp("Clock", "com.clock", ComponentName("com.clock", "Main"), themeColor = 0xFFFF0097.toInt()),
        InstalledApp("Settings", "com.set", ComponentName("com.set", "Main"), themeColor = 0xFF9F00A7.toInt()),
        InstalledApp("Phone", "com.phone", ComponentName("com.phone", "Main"), themeColor = 0xFF7E3878.toInt()),
        InstalledApp("Messaging", "com.msg", ComponentName("com.msg", "Main"), themeColor = 0xFF1D1D1D.toInt()),
        InstalledApp("Browser", "com.browser", ComponentName("com.browser", "Main"), themeColor = 0xFF00A300.toInt())
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Dark theme was default for WP8
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        ModernTopBar(date = "Thu · Jun 11")
        Spacer(Modifier.height(32.dp))
        QuickAppsBar(apps = apps, onAppClick = {})
    }
}
