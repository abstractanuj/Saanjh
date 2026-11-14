package com.geecee.escapelauncher.feature.widgets.ui

import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.geecee.escapelauncher.feature.widgets.KhojSideEffect
import com.geecee.escapelauncher.feature.widgets.AppIconFetcher
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import coil3.compose.rememberAsyncImagePainter
import com.geecee.escapelauncher.core.model.ContactData
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.composables.AutoResizingText
import com.geecee.escapelauncher.core.ui.composables.bouncyMorphable
import com.geecee.escapelauncher.core.ui.composables.glassmorphicOverlay
import com.geecee.escapelauncher.feature.widgets.KhojSearchViewModel

@Composable
fun KhojSearchScreen(
    onDismiss: () -> Unit,
    viewModel: KhojSearchViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsState()
    val apps by viewModel.filteredApps.collectAsState()
    val contacts by viewModel.filteredContacts.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val imageLoader = remember(context, viewModel.iconPackManager) {
        ImageLoader.Builder(context)
            .components {
                add(AppIconFetcher.Factory(context, viewModel.iconPackManager))
            }
            .build()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(viewModel.sideEffects) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is KhojSideEffect.LaunchApp -> {
                    keyboardController?.hide()
                    onDismiss()
                    try {
                        val launcherApps = context.getSystemService(android.content.Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                        launcherApps.startMainActivity(effect.app.componentName, effect.app.user, null, null)
                    } catch (e: SecurityException) {
                        Toast.makeText(context, com.geecee.escapelauncher.core.ui.R.string.error_cannot_launch_app, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        val intent = context.packageManager.getLaunchIntentForPackage(effect.app.packageName)
                        if (intent != null) {
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, com.geecee.escapelauncher.core.ui.R.string.error_app_not_found, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                is KhojSideEffect.DialNumber -> {
                    keyboardController?.hide()
                    onDismiss()
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${effect.phoneNumber}")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, com.geecee.escapelauncher.core.ui.R.string.error_cannot_open_dialer, Toast.LENGTH_SHORT).show()
                    }
                }
                is KhojSideEffect.LaunchWhatsApp -> {
                    keyboardController?.hide()
                    onDismiss()
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${effect.phoneNumber}")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, com.geecee.escapelauncher.core.ui.R.string.error_cannot_open_whatsapp, Toast.LENGTH_SHORT).show()
                    }
                }
                is KhojSideEffect.ShowError -> {
                    Toast.makeText(context, effect.messageId, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    keyboardController?.hide()
                    onDismiss()
                }
            )
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .graphicsLayer { clip = false }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Consume clicks so they don't dismiss
                )
        ) {
            // LAYER 1 (Background): Feathered Gradient Cloud
            FeatheredGradientCloudBackground(
                modifier = Modifier.matchParentSize()
            )

            // LAYER 2 (Foreground): Solid White Results & Search Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 36.dp, bottom = 20.dp)
            ) {
                // Results above search bar
                if (apps.isNotEmpty() || contacts.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                    ) {
                        if (apps.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(apps, key = { "${it.packageName}_${it.user.hashCode()}" }) { app ->
                                    AppResultItem(
                                        app = app,
                                        imageLoader = imageLoader,
                                        onClick = { viewModel.onAppClicked(app) }
                                    )
                                }
                            }
                        }

                        if (contacts.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)
                            ) {
                                items(contacts, key = { it.id }) { contact ->
                                    ContactResultItem(
                                        contact = contact,
                                        onCallClick = { viewModel.onContactCallClicked(contact) },
                                        onWhatsappClick = { viewModel.onContactWhatsAppClicked(contact) }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.2f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                        )
                    }
                }

                // Bottom Search Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = query,
                            onValueChange = viewModel::onSearchQueryChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Color.White),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController?.hide()
                                onDismiss()
                            })
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChanged("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val CLOUD_SHADER = """
    uniform float2 resolution;
    uniform float time;
    uniform float isNight;
    layout(color) uniform half4 color1;
    layout(color) uniform half4 color2;
    layout(color) uniform half4 color3;

    float hash(vec2 p) {
        return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
    }

    float noise(vec2 p) {
        vec2 i = floor(p);
        vec2 f = fract(p);
        f = f * f * (3.0 - 2.0 * f);
        float a = hash(i);
        float b = hash(i + vec2(1.0, 0.0));
        float c = hash(i + vec2(0.0, 1.0));
        float d = hash(i + vec2(1.0, 1.0));
        return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    }

    float fbm(vec2 p) {
        float v = 0.0;
        float a = 0.5;
        for (int i = 0; i < 4; i++) {
            v += a * noise(p);
            p *= 2.0;
            a *= 0.5;
        }
        return v;
    }

    half4 main(in float2 fragCoord) {
        vec2 uv = (fragCoord - resolution.xy * 0.5) / (min(resolution.x, resolution.y) * 0.5);
        
        float d = length(uv);
        float angle = atan(uv.y, uv.x);
        
        float n = fbm(uv * 1.5 + time * 0.05);
        float wobble = sin(angle * 2.0 + time * 0.1) * 0.05 + cos(angle * 3.0 - time * 0.07) * 0.03 + n * 0.1;
        d += wobble;
        
        float core = exp(-d * 2.5);
        float halo = exp(-d * 1.0) * 0.6;
        
        vec4 c = mix(color3, color2, halo);
        c = mix(c, color1, core);
        
        // Add whitish stars for night mode
        float starNoise = hash(floor(uv * min(resolution.x, resolution.y) * 0.1));
        float stars = step(0.98, starNoise) * isNight * (sin(time * 3.0 + starNoise * 100.0) * 0.5 + 0.5);
        c.rgb += vec3(stars * 0.6);
        
        // Circular atom cloud fade out
        float edgeAlpha = 1.0 - smoothstep(0.4, 1.1, d);
        c.a *= edgeAlpha;
        
        // Premultiply alpha
        c.rgb *= c.a;
        
        return half4(c);
    }
"""

@Composable
private fun FeatheredGradientCloudBackground(modifier: Modifier = Modifier) {
    val calendar = java.util.Calendar.getInstance()
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    
    val isNight = hour >= 19 || hour <= 4
    
    val (c1, c2, c3) = when (hour) {
        in 5..10 -> Triple(Color(0xFFF9A8D4).copy(alpha = 0.95f), Color(0xFFFBCFE8).copy(alpha = 0.80f), Color(0xFFFDF2F8).copy(alpha = 0.40f)) // Morning (pinkish)
        in 11..15 -> Triple(Color(0xFF38BDF8).copy(alpha = 0.95f), Color(0xFF7DD3FC).copy(alpha = 0.80f), Color(0xFFE0F2FE).copy(alpha = 0.40f)) // Noon (light blue)
        in 16..18 -> Triple(Color(0xFFFB923C).copy(alpha = 0.95f), Color(0xFFFDBA74).copy(alpha = 0.80f), Color(0xFFFFEDD5).copy(alpha = 0.40f)) // Evening (orangish)
        else -> Triple(Color(0xFF1E3A8A).copy(alpha = 0.95f), Color(0xFF3B82F6).copy(alpha = 0.80f), Color(0xFFBFDBFE).copy(alpha = 0.40f)) // Dusk/Night (bluish, whitish)
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { android.graphics.RuntimeShader(CLOUD_SHADER) }
        val transition = rememberInfiniteTransition(label = "cloudTransition")
        val time by transition.animateFloat(
            initialValue = 0f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(50000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time"
        )

        Canvas(modifier = modifier.graphicsLayer { clip = false }) {
            if (size.width <= 0f || size.height <= 0f) return@Canvas
            
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", time)
            shader.setFloatUniform("isNight", if (isNight) 1f else 0f)
            shader.setColorUniform("color1", c1.toArgb())
            shader.setColorUniform("color2", c2.toArgb())
            shader.setColorUniform("color3", c3.toArgb())
            
            drawRect(brush = ShaderBrush(shader))
        }
    } else {
        Canvas(modifier = modifier.graphicsLayer { clip = false }) {
            if (size.width <= 0f || size.height <= 0f) return@Canvas
            
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension * 0.8f
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to c1.copy(alpha = 0.9f),
                    0.5f to c2.copy(alpha = 0.6f),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
fun AppResultItem(
    app: InstalledApp,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .bouncyMorphable(
                restRadius = 24.dp,
                pressedRadius = 12.dp,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(app)
                .build(),
            imageLoader = imageLoader,
            contentDescription = app.displayName,
            modifier = Modifier.size(56.dp)
        )
    }
}

@Composable
fun ContactResultItem(
    contact: ContactData,
    onCallClick: () -> Unit,
    onWhatsappClick: () -> Unit
) {
    val crispTextShadow = Shadow(
        color = Color.Black.copy(alpha = 0.28f),
        offset = Offset(0f, 1.5f),
        blurRadius = 3f
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .bouncyMorphable(
                restRadius = 36.dp, // more circular pill for contacts
                pressedRadius = 16.dp,
                onClick = onCallClick // default action
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AutoResizingText(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge.copy(shadow = crispTextShadow),
                color = Color.White
            )
            AutoResizingText(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.labelMedium.copy(shadow = crispTextShadow),
                color = Color.White.copy(alpha = 0.92f)
            )
        }
        
        IconButton(onClick = onCallClick) {
            Icon(
                imageVector = Icons.Rounded.Call,
                contentDescription = "Call",
                tint = Color.White
            )
        }
        
        if (contact.hasWhatsApp) {
            IconButton(onClick = onWhatsappClick) {
                @Suppress("DEPRECATION")
                Icon(
                    imageVector = Icons.Rounded.Message, // Using message as generic whatsapp placeholder
                    contentDescription = "WhatsApp",
                    tint = Color(0xFF25D366) // WhatsApp Green
                )
            }
        }
    }
}
