package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.core.theme.CardContainerColor
import com.geecee.escapelauncher.core.theme.ContentColor


/**
 * A setting item with a label on the left and a SingleChoiceSegmentedButtonRow on the right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSingleChoiceSegmentedButtons(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isTopOfGroup: Boolean = false, // Compatibility
    isBottomOfGroup: Boolean = false // Compatibility
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium, // Celebrate Typography
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                options.forEachIndexed { index, optionLabel ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index, count = options.size
                        ), onClick = {
                            onSelectedIndexChange(index)
                        }, selected = index == selectedIndex
                    ) {
                        Text(text = optionLabel, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                }
            }
        }
    }
}

/**
 * A setting item with a label on the left, a Slider in the middle, and a reset Icon on the right.
 * Visually styled to match SettingsSingleChoiceSegmentedButtons.
 *
 * @param label The text for the label displayed to the left of the slider.
 * @param value The current value of the slider.
 * @param onValueChange Callback that is invoked when the slider's value changes.
 * @param valueRange The range of values that the slider can take.
 * @param steps The number of discrete steps the slider can snap to between the min and max values.
 * @param onReset Callback that is invoked when the reset icon is clicked.
 * @param modifier Optional [Modifier] for this composable.
 * @param isTopOfGroup Whether this item is the first in a group of settings, for corner rounding.
 * @param isBottomOfGroup Whether this item is the last in a group of settings, for corner rounding.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    isTopOfGroup: Boolean = false, // Kept for compatibility
    isBottomOfGroup: Boolean = false, // Kept for compatibility
    resetButtonContentDescription: String
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium, // Celebrate Typography
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = resetButtonContentDescription,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(start = 8.dp)
                        .clickable(onClick = onReset),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
