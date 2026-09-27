package com.geecee.escapelauncher.feature.settings.folders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.model.AppCategory
import com.geecee.escapelauncher.core.ui.composables.BulkManager
import com.geecee.escapelauncher.feature.settings.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun ManageFolderAppsScreen(
    categoryName: String,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val category = AppCategory.fromString(categoryName)
    val installedApps by viewModel.installedApps.collectAsState()
    val categoriesMap by viewModel.modifiedAppsRepository.getCategoriesMapFlow().collectAsState(initial = emptyMap())
    val coroutineScope = rememberCoroutineScope()

    val categoryAppIds = categoriesMap.filter { it.value == category.name }.keys

    BulkManager(
        items = installedApps,
        id = { it.packageName },
        label = { it.displayName },
        selectedIdsOverride = categoryAppIds,
        title = "Apps in ${category.displayName}",
        onBackClicked = onBack,
        onItemClicked = { app, isSelected ->
            coroutineScope.launch {
                if (isSelected) {
                    // Remove from category (reset to null or OTHERS)
                    viewModel.modifiedAppsRepository.setCategory(app.packageName, null)
                } else {
                    // Add to category
                    viewModel.modifiedAppsRepository.setCategory(app.packageName, category.name)
                }
            }
        }
    )
}
