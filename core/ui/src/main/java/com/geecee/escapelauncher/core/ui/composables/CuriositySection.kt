package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.model.CuriosityContent

@Composable
fun CuriositySection(
    content: CuriosityContent,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val text = when (content) {
        is CuriosityContent.Question -> content.text
        is CuriosityContent.Music -> content.text
        is CuriosityContent.Movie -> content.text
        is CuriosityContent.None -> ""
    }

    if (text.isBlank()) return

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Adaptive size: Scaled based on screen width but bounded to bodyLarge range
    val adaptiveFontSize = (screenWidth / 25f).coerceIn(14f, 18f).sp
    val adaptiveLineHeight = (adaptiveFontSize.value * 1.4f).sp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
            .clickable(enabled = content !is CuriosityContent.Question) { onClick() },
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = adaptiveFontSize,
                lineHeight = adaptiveLineHeight,
                fontWeight = FontWeight.Normal,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun CuriositySectionPreview() {
    MaterialTheme {
        CuriositySection(content = CuriosityContent.Question("How do dolphins jump so high?"))
    }
}
