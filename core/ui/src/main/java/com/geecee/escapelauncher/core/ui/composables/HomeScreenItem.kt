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
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.primaryContentColor


/**
 * An item displayed on the HomeScreen or Apps list
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenItem(
    modifier: Modifier = Modifier,
    appName: String,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
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
                onLongClick = onAppLongClick
            )
            .fillMaxWidth()
    ) {
        // App name text with click and long click handlers
        Text(
            appName,
            modifier = Modifier.padding(vertical = 15.dp),
            color = primaryContentColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}