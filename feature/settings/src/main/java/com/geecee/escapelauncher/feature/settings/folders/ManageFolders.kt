package com.geecee.escapelauncher.feature.settings.folders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.model.AppCategory
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.SettingsNavigationItem

@Composable
fun ManageFoldersScreen(
    onBack: () -> Unit,
    onCategoryClick: (AppCategory) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding()
    ) {
        item {
            EscapeHeader(onBack, "Manage Folders")
        }

        item {
            Text(
                text = "Select a folder to manage its apps.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }

        items(AppCategory.entries) { category ->
            SettingsNavigationItem(
                label = category.displayName,
                onClick = { onCategoryClick(category) }
            )
        }
        
        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}
