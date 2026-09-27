package com.geecee.escapelauncher.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.feature.settings.mainpage.MainSettingsPageViewModel

@Composable
fun CustomYoutubeChannelsPage(
    onBack: () -> Unit,
    viewModel: MainSettingsPageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdding by viewModel.isAddingChannel.collectAsState()
    var newChannelId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        EscapeHeader(onBack, "Custom YouTube Channels")

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Add YouTube Channel IDs to include them in Today's Picks. You can find the ID in the channel URL (e.g., UCauuunt6oDekWe6v1NGQxUg).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newChannelId,
                    onValueChange = { newChannelId = it },
                    label = { Text("Channel ID") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isAdding
                )
                Spacer(Modifier.width(8.dp))
                if (isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    IconButton(
                        onClick = {
                            if (newChannelId.isNotBlank()) {
                                viewModel.addCustomYoutubeChannel(newChannelId.trim())
                                newChannelId = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Current Channels",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.customYoutubeChannels) { channel ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = channel.id,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            IconButton(onClick = { viewModel.removeCustomYoutubeChannel(channel.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
                
                if (uiState.customYoutubeChannels.isEmpty() && !isAdding) {
                    item {
                        Text(
                            text = "No custom channels added yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
