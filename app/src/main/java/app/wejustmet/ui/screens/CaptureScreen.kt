package app.wejustmet.ui.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wejustmet.core.AppConfig
import app.wejustmet.core.ContactDraft
import app.wejustmet.core.DEMO_CAPTURE_RESULT
import app.wejustmet.ui.Tokens
import kotlinx.coroutines.delay

/** Orb visual spec locked to design/orb-preview.html (PLAN design language). */
private object OrbSpec {
    val ORB_SIZE = 150.dp
    const val BREATHE_SCALE = 1.06f
    const val BREATHE_MS = 2600
    const val HALO_MAX_SCALE = 1.75f
    const val HALO_START_ALPHA = 0.7f
    const val HALO_SECOND_DELAY_MS = 1300
    const val GRADIENT_CENTER_X = 0.32f
    const val GRADIENT_CENTER_Y = 0.28f
    const val GRADIENT_MID_STOP = 0.45f
}

@Composable
fun CaptureScreen(onCaptured: (ContactDraft) -> Unit, onMenu: () -> Unit) {
    var countingDown by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onMenu,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(app.wejustmet.R.string.action_menu),
                tint = Tokens.BrandGreen,
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(96.dp))
            Text(
                text = AppConfig.CAPTURE_PROMPT_TITLE,
                color = Tokens.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = AppConfig.CAPTURE_PROMPT_LINE,
                color = Tokens.TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(44.dp))
            OrbStage(onTap = { countingDown = true })
        }

        Text(
            text = AppConfig.CAPTURE_HINT,
            color = Tokens.TextMuted,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
        )

        if (countingDown) {
            CountdownOverlay(onFinished = { onCaptured(DEMO_CAPTURE_RESULT) })
        }
    }
}

@Composable
private fun OrbStage(onTap: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "orb")
    val breathe by transition.animateFloat(
        initialValue = 1f,
        targetValue = OrbSpec.BREATHE_SCALE,
        animationSpec = infiniteRepeatable(
            tween(OrbSpec.BREATHE_MS / 2),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Halo(transition, delayMs = 0)
        Halo(transition, delayMs = OrbSpec.HALO_SECOND_DELAY_MS)
        Box(
            modifier = Modifier
                .size(OrbSpec.ORB_SIZE)
                .graphicsLayer {
                    scaleX = breathe
                    scaleY = breathe
                }
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Tokens.BrandPrimaryHover,
                                OrbSpec.GRADIENT_MID_STOP to Tokens.BrandPrimary,
                                1f to Tokens.BrandGreen,
                            ),
                            center = Offset(
                                size.width * OrbSpec.GRADIENT_CENTER_X,
                                size.height * OrbSpec.GRADIENT_CENTER_Y,
                            ),
                            radius = size.width,
                        ),
                    )
                }
                .clickable(onClick = onTap),
        )
    }
}

@Composable
private fun Halo(
    transition: androidx.compose.animation.core.InfiniteTransition,
    delayMs: Int,
) {
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(OrbSpec.BREATHE_MS, easing = LinearOutSlowInEasing),
            initialStartOffset = StartOffset(delayMs),
        ),
        label = "halo",
    )
    Box(
        modifier = Modifier
            .size(OrbSpec.ORB_SIZE)
            .graphicsLayer {
                val scale = 1f + (OrbSpec.HALO_MAX_SCALE - 1f) * progress
                scaleX = scale
                scaleY = scale
                alpha = OrbSpec.HALO_START_ALPHA * (1f - progress)
            }
            .drawBehind {
                drawCircle(
                    color = Tokens.BrandPrimary,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                )
            },
    )
}

@Composable
private fun CountdownOverlay(onFinished: () -> Unit) {
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        for (index in AppConfig.COUNTDOWN_STEPS.indices) {
            step = index
            delay(AppConfig.COUNTDOWN_BEAT_MS)
        }
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.BgPrimary.copy(alpha = 0.96f))
            .clickable(enabled = false) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(
            text = AppConfig.COUNTDOWN_READY_LINE,
            color = Tokens.BrandPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = AppConfig.COUNTDOWN_STEPS[step],
            color = Tokens.BrandGreen,
            fontSize = 110.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = AppConfig.COUNTDOWN_SUB_LINE,
            color = Tokens.TextMuted,
            fontSize = 14.sp,
        )
    }
}
