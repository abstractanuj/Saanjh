package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A designer-styled settings group container.
 * Groups multiple settings into a single surface with dividers.
 */
@Composable
fun SettingsGroup(
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

/**
 * A divider for within a SettingsGroup.
 */
@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

/**
 * Designer-styled switch item for use within a SettingsGroup.
 */
@Composable
fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isTopOfGroup: Boolean = false, // Kept for compatibility
    isBottomOfGroup: Boolean = false // Kept for compatibility
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

/**
 * Designer-styled navigation item for use within a SettingsGroup.
 */
@Composable
fun SettingsNavigationItem(
    label: String,
    onClick: () -> Unit,
    diagonalArrow: Boolean = false,
    modifier: Modifier = Modifier,
    isTopOfGroup: Boolean = false, // Kept for compatibility
    isBottomOfGroup: Boolean = false, // Kept for compatibility
    repalceIconWichCheck: Boolean = false // Kept for compatibility
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .defaultMinSize(minHeight = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (repalceIconWichCheck) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (diagonalArrow) -45f else 0f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Designer-styled text field for use within a SettingsGroup.
 */
@Composable
fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isTopOfGroup: Boolean = false, // Kept for compatibility
    isBottomOfGroup: Boolean = false // Kept for compatibility
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            singleLine = true
        )
    }
}

/**
 * Swipeable button (Restored for compatibility)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwipeableButton(
    label: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
    deleteIconContentDescription: String
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.5f }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleteClick()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.padding(vertical = 4.dp),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(Icons.Default.Delete, contentDescription = deleteIconContentDescription, tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

/**
 * Old components kept for compatibility/temporary until fully migrated.
 */
@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    isTopOfGroup: Boolean = false,
    isBottomOfGroup: Boolean = false,
    fontFamily: FontFamily? = MaterialTheme.typography.bodyMedium.fontFamily,
    isDisabled: Boolean = false,
    useAutoResize: Boolean = true,
    isSelected: Boolean = false
) {
    val containerColor = if (isDisabled) MaterialTheme.colorScheme.surfaceVariant else if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        enabled = !isDisabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(label, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SettingsSpacer() {
    Spacer(modifier = Modifier.height(24.dp))
}
