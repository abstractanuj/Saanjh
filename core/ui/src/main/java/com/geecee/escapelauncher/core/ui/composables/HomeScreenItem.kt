package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.primaryContentColor


import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import coil3.compose.rememberAsyncImagePainter
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext

/**
 * An item displayed on the HomeScreen or Apps list
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenItem(
    modifier: Modifier = Modifier,
    appName: String,
    packageName: String? = null,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    val context = LocalContext.current
    val pm = context.packageManager
    
    // In a full implementation, this would look up the custom icon pack via SharedPreferences/DataStore
    // and attempt to load the mapping for `packageName`. Here we provide the default system icon as a fallback.
    val appIcon = remember(packageName) {
        packageName?.let { 
            try { 
                pm.getApplicationIcon(it) 
            } catch (_: PackageManager.NameNotFoundException) { 
                null 
            } 
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (alignment) {
            Alignment.Start -> Arrangement.Start
            Alignment.CenterHorizontally -> Arrangement.Center
            Alignment.End -> Arrangement.End
            else -> Arrangement.Center
        },
        modifier = modifier
            .combinedClickable(
                onClick = onAppClick,
                onLongClick = onAppLongClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Alive in motion padding space if pressed? Or just base padding.
    ) {
        if (appIcon != null) {
            Image(
                painter = rememberAsyncImagePainter(appIcon),
                contentDescription = "$appName icon",
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
        }

        // App name text with click and long click handlers
        Text(
            appName,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = if (appIcon != null) 0.dp else 16.dp), // Delightful use of whitespace
            color = primaryContentColor,
            style = MaterialTheme.typography.headlineSmall.copy( // Celebrate Typography: larger, clearer scale
                fontWeight = FontWeight.Light
            )
        )
    }
}