package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Theme-aware High-precision Search Bar
 */
@Composable
fun AnimatedPillSearchBar(
    closedText: String,
    searchText: String,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onSearchDone: (String, SoftwareKeyboardController?) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    showBackground: Boolean = true
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(searchText)) }

    LaunchedEffect(searchText) {
        if (searchText != textFieldValue.text) {
            // Update the text while preserving selection/cursor if possible
            textFieldValue = textFieldValue.copy(
                text = searchText,
                selection = textFieldValue.selection
            )
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isExpanded, autoFocus) {
        if (isExpanded && autoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = MaterialTheme.colorScheme.onSurface
    val placeholderColor = contentColor.copy(alpha = 0.4f)

    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isExpanded) { onExpandedChange(true) }
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = if (showBackground) containerColor else Color.Transparent,
        border = if (showBackground) androidx.compose.foundation.BorderStroke(
            1.dp,
            contentColor.copy(alpha = 0.08f)
        ) else null,
        shadowElevation = 0.dp // Standard look, no shadow
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        text = closedText,
                        color = placeholderColor,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            letterSpacing = 0.sp
                        ),
                    )
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        if (newValue.text != searchText) {
                            onSearchTextChanged(newValue.text)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onSearchDone(textFieldValue.text.trim(), keyboardController)
                    }),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = contentColor.copy(alpha = 0.9f),
                        fontSize = 18.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }

            if (textFieldValue.text.isNotEmpty()) {
                IconButton(
                    onClick = {
                        textFieldValue = TextFieldValue("")
                        onSearchTextChanged("")
                        keyboardController?.hide()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
