package com.geecee.escapelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.geecee.escapelauncher.core.data.repository.LocalContent

class GlassmorphicReminderActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val quoteText = intent.getStringExtra("QUOTE_TEXT")
        val quoteAuthor = intent.getStringExtra("QUOTE_AUTHOR")

        val displayQuote = if (quoteText != null) {
            com.geecee.escapelauncher.core.data.repository.Quote(quoteText, quoteAuthor ?: "Escape")
        } else {
            LocalContent.parsedQuotes.random()
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    ReminderPopup(
                        text = displayQuote.text,
                        author = displayQuote.author,
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderPopup(text: String, author: String, onDismiss: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    // Prevent back button from dismissing
    BackHandler(enabled = true) {}

    Dialog(
        onDismissRequest = { /* Do nothing, force button click */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(270.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xD6F5F1EB)) // #F5F1EB at 84% opacity
                .border(0.5.dp, Color(0x59FFFFFF), RoundedCornerShape(20.dp)) // Translucent white hairline highlight
                // Note: Heavy blur applies to background behind dialog ideally, 
                // but this background tint brings the warm alabaster feel.
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Content Area
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = "Mindful Moment",
                        fontSize = 17.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = Color(0xFF1E1B18), // Deep espresso-charcoal
                        letterSpacing = (-0.4).sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = text,
                        fontSize = 14.5.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                        color = Color(0xFF383431), // Muted warm charcoal
                        letterSpacing = (-0.2).sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 19.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (author.isNotBlank() && author != "Unknown" && author != "Escape") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "— $author",
                            fontSize = 12.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                            color = Color(0xFF6B6661), 
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Hairline Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(Color(0x243C2D1E)) // rgba(60, 45, 30, 0.14)
                )
                
                // Bottom Button Area
                TextButton(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss() 
                    },
                    contentPadding = PaddingValues(0.dp),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(
                        text = "Okay",
                        fontSize = 17.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = Color(0xFF007AFF) // Classic iOS blue
                    )
                }
            }
        }
    }
}
