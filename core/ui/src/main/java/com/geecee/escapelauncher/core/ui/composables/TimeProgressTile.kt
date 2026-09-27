package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geecee.escapelauncher.core.theme.CardContainerColor
import com.geecee.escapelauncher.core.theme.ContentColor
import java.util.*

@Composable
fun TimeProgressTile(
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    
    val dayProgress = calendar.get(Calendar.HOUR_OF_DAY) / 24f
    val monthProgress = calendar.get(Calendar.DAY_OF_MONTH) / calendar.getActualMaximum(Calendar.DAY_OF_MONTH).toFloat()
    val yearProgress = calendar.get(Calendar.DAY_OF_YEAR) / calendar.getActualMaximum(Calendar.DAY_OF_YEAR).toFloat()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardContainerColor)
            .padding(16.dp)
            .fillMaxWidth()
            .height(120.dp)
            .animateContentSize()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProgressRow("Day", dayProgress)
            ProgressRow("Month", monthProgress)
            ProgressRow("Year", yearProgress)
        }
    }
}

@Composable
private fun ProgressRow(label: String, progress: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 10.sp, color = ContentColor.copy(alpha = 0.6f))
            Text("${(progress * 100).toInt()}%", fontSize = 10.sp, color = ContentColor.copy(alpha = 0.6f))
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = ContentColor,
            trackColor = ContentColor.copy(alpha = 0.1f),
        )
    }
}

@Preview
@Composable
fun TimeProgressTilePreview() {
    Box(Modifier.padding(16.dp)) {
        TimeProgressTile()
    }
}
