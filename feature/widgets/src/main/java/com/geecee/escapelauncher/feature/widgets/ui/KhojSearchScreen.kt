package com.geecee.escapelauncher.feature.widgets.ui

import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.geecee.escapelauncher.core.model.ContactData
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.composables.AutoResizingText
import com.geecee.escapelauncher.core.ui.composables.bouncyMorphable
import com.geecee.escapelauncher.feature.widgets.AppIconFetcher
import com.geecee.escapelauncher.feature.widgets.KhojSearchViewModel
import com.geecee.escapelauncher.feature.widgets.KhojSideEffect

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
            // LAYER 1 (Background): Atomic Cloud with Night Stars & Dynamic Sizing
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
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
                            color = Color.White.copy(alpha = 0.24f),
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
                                .focusRequester(focusRequester)
                                .testTag("khoj_search_input"),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Color.White),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                val topApp = apps.firstOrNull()
                                if (topApp != null) {
                                    viewModel.onAppClicked(topApp)
                                } else {
                                    keyboardController?.hide()
                                    onDismiss()
                                }
                            })
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChanged("") },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("khoj_clear_button")
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

/**
 * Mathematically optimized Atomic Cloud AGSL shader:
 * Replaces per-pixel inverse trigonometry `atan(uv.y, uv.x)` with Chebyshev polynomial
 * vector identities on normalized coordinates `u = uv / max(d, 0.0001)` and uses 3-octave FBM,
 * preserving the exact Atomic Cloud, twinkling night stars, and dynamic sizing.
 */
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
        for (int i = 0; i < 3; i++) {
            v += a * noise(p);
            p *= 2.0;
            a *= 0.5;
        }
        return v;
    }

    half4 main(in float2 fragCoord) {
        float minRes = min(resolution.x, resolution.y);
        // Wide elliptical normalization so the atom cloud spans across all horizontal icons & search input
        vec2 uv = (fragCoord - resolution.xy * 0.5) / vec2(resolution.x * 0.62, resolution.y * 0.58);

        float d = length(uv);
        vec2 u = uv / max(d, 0.0001);

        // Chebyshev harmonic identities instead of costly per-pixel atan(uv.y, uv.x)
        float cos2a = u.x * u.x - u.y * u.y;
        float sin2a = 2.0 * u.x * u.y;
        float cos3a = u.x * (4.0 * u.x * u.x - 3.0);
        float sin3a = u.y * (3.0 - 4.0 * u.y * u.y);

        float t1 = time * 0.1;
        float t2 = time * 0.07;
        float wave2 = sin2a * cos(t1) + cos2a * sin(t1);
        float wave3 = cos3a * cos(t2) + sin3a * sin(t2);

        float n = fbm(uv * 1.5 + time * 0.05);
        float wobble = wave2 * 0.06 + wave3 * 0.04 + n * 0.12;
        d += wobble;

        float core = exp(-d * 1.55);
        float halo = exp(-d * 0.68) * 0.85;

        vec4 c = mix(color3, color2, halo);
        c = mix(c, color1, core);

        // Twinkling whitish stars for night mode
        float starNoise = hash(floor(uv * minRes * 0.1));
        float stars = step(0.98, starNoise) * isNight * (sin(time * 3.0 + starNoise * 100.0) * 0.5 + 0.5);
        c.rgb += vec3(stars * 0.6);

        // Wide atom cloud fade out
        float edgeAlpha = 1.0 - smoothstep(0.55, 1.25, d);
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

    // Rich, saturated, high-contrast palettes so pure white line icons & text are vividly legible over white backgrounds
    val (c1, c2, c3) = when (hour) {
        in 5..10 -> Triple(
            Color(0xFFBE185D).copy(alpha = 0.98f), // Deep Rose Berry core
            Color(0xFF9333EA).copy(alpha = 0.92f), // Vibrant Orchid Violet mid
            Color(0xFF6B21A8).copy(alpha = 0.82f)  // Rich Plum outer halo
        )
        in 11..15 -> Triple(
            Color(0xFF0369A1).copy(alpha = 0.98f), // Deep Ocean Cobalt core
            Color(0xFF0284C7).copy(alpha = 0.92f), // Vibrant Azure Cyan mid
            Color(0xFF1E40AF).copy(alpha = 0.82f)  // Rich Sapphire outer halo
        )
        in 16..18 -> Triple(
            Color(0xFFC2410C).copy(alpha = 0.98f), // Deep Burnt Amber-Crimson core
            Color(0xFFEA580C).copy(alpha = 0.92f), // Vibrant Sunset Tangerine mid
            Color(0xFF7C2D12).copy(alpha = 0.82f)  // Rich Dusk Terracotta outer halo
        )
        else -> Triple(
            Color(0xFF0F172A).copy(alpha = 0.98f), // Deep Cosmic Obsidian core
            Color(0xFF1E3A8A).copy(alpha = 0.92f), // Rich Midnight Indigo mid
            Color(0xFF1D4ED8).copy(alpha = 0.82f)  // Deep Royal Blue outer halo
        )
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
            val radius = maxOf(size.width, size.height) * 0.72f
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to c1,
                    0.55f to c2,
                    0.85f to c3,
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
                restRadius = 36.dp,
                pressedRadius = 16.dp,
                onClick = onCallClick
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

        IconButton(
            onClick = onCallClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Call,
                contentDescription = "Call",
                tint = Color.White
            )
        }

        if (contact.hasWhatsApp) {
            IconButton(
                onClick = onWhatsappClick,
                modifier = Modifier.size(48.dp)
            ) {
                @Suppress("DEPRECATION")
                Icon(
                    imageVector = Icons.Rounded.Message,
                    contentDescription = "WhatsApp",
                    tint = Color(0xFF25D366)
                )
            }
        }
    }
}
