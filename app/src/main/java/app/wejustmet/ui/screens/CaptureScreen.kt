package app.wejustmet.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.wejustmet.R
import app.wejustmet.core.AppConfig
import app.wejustmet.core.ContactDraft
import app.wejustmet.data.SpeechCapture
import app.wejustmet.send.WhatsAppSender
import app.wejustmet.ui.Tokens
import java.io.File
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** Orb visual spec locked to design/orb-preview.html (PLAN design language). */
private object OrbSpec {
    val ORB_SIZE = 150.dp
    val GOLD_RING_SIZE = 178.dp
    const val BREATHE_SCALE = 1.06f
    const val BREATHE_MS = 2600
    const val BREATHE_HEARING_MS = 1100
    const val HALO_MAX_SCALE = 1.75f
    const val HALO_START_ALPHA = 0.7f
    const val HALO_SECOND_DELAY_MS = 1300
    const val GRADIENT_CENTER_X = 0.32f
    const val GRADIENT_CENTER_Y = 0.28f
    const val GRADIENT_MID_STOP = 0.45f
    const val HEARING_HOLD_MS = 900L
    const val HEARING_TICK_MS = 250L
}

private const val STOP_RESULT_GRACE_MS = 900L

/**
 * TakePicture with NO_HISTORY: the camera activity must never linger on our task stack,
 * otherwise leaving mid-capture makes the launcher icon reopen straight into the camera.
 */
private class TakeSelfieContract : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: android.content.Context, input: android.net.Uri) =
        super.createIntent(context, input)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NO_HISTORY)
}

@Composable
fun CaptureScreen(
    extract: suspend (String) -> ContactDraft,
    onCaptured: (draft: ContactDraft, selfie: File?) -> Unit,
    onMenu: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var transcript by remember { mutableStateOf("") }
    var lastHeardAt by remember { mutableLongStateOf(0L) }
    var hearing by remember { mutableStateOf(false) }
    var countingDown by remember { mutableStateOf(false) }
    var extraction by remember { mutableStateOf<Deferred<ContactDraft>?>(null) }
    var pendingDraft by remember { mutableStateOf<ContactDraft?>(null) }

    // PLAN camera fallback path: system camera writes into the FileProvider cache slot.
    val takeSelfie = rememberLauncherForActivityResult(
        TakeSelfieContract(),
    ) { captured ->
        val file = WhatsAppSender.selfieFile(context)
        val draft = pendingDraft ?: ContactDraft(note = transcript.trim())
        onCaptured(draft, file.takeIf { captured && it.length() > 0 })
    }
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val beginExtraction: (String) -> Unit = { text ->
        if (extraction == null) {
            extraction = scope.async {
                runCatching { extract(text) }.getOrElse { ContactDraft(note = text.trim()) }
            }
            countingDown = true
        }
    }

    val speech = remember {
        SpeechCapture(
            context = context,
            onTranscript = { transcript = it },
            onHearing = { lastHeardAt = SystemClock.elapsedRealtime() },
            onFinished = { text -> beginExtraction(text) },
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> micGranted = granted }

    LaunchedEffect(micGranted) {
        if (micGranted) speech.start() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    DisposableEffect(Unit) {
        onDispose { speech.destroy() }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(OrbSpec.HEARING_TICK_MS)
            hearing = SystemClock.elapsedRealtime() - lastHeardAt < OrbSpec.HEARING_HOLD_MS
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onMenu,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(R.string.action_menu),
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
            key(hearing) {
                OrbStage(
                    hearing = hearing,
                    onTap = {
                        if (!countingDown) {
                            speech.stop()
                            scope.launch {
                                delay(STOP_RESULT_GRACE_MS)
                                beginExtraction(transcript)
                            }
                        }
                    },
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                text = transcript,
                color = Tokens.TextMuted,
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState(), reverseScrolling = true)
                    .padding(bottom = 72.dp),
            )
        }

        Text(
            text = if (hearing) AppConfig.CAPTURE_HEARING_CAPTION else AppConfig.CAPTURE_HINT,
            color = if (hearing) Tokens.AccentGoldText else Tokens.TextMuted,
            fontSize = 13.sp,
            fontWeight = if (hearing) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
        )

        if (countingDown) {
            CountdownOverlay(
                onFinished = {
                    scope.launch {
                        val draft = withTimeoutOrNull(AppConfig.EXTRACTION_TIMEOUT_MS) {
                            extraction?.await()
                        } ?: ContactDraft(note = transcript.trim())
                        pendingDraft = draft
                        runCatching { takeSelfie.launch(WhatsAppSender.selfieUri(context)) }
                            .onFailure { onCaptured(draft, null) }
                    }
                },
            )
        }
    }
}

@Composable
private fun OrbStage(hearing: Boolean, onTap: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "orb")
    val breatheMs = if (hearing) OrbSpec.BREATHE_HEARING_MS else OrbSpec.BREATHE_MS
    val breathe by transition.animateFloat(
        initialValue = 1f,
        targetValue = OrbSpec.BREATHE_SCALE,
        animationSpec = infiniteRepeatable(
            tween(breatheMs / 2),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathe",
    )

    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        Halo(transition, delayMs = 0)
        Halo(transition, delayMs = OrbSpec.HALO_SECOND_DELAY_MS)
        if (hearing) {
            Box(
                modifier = Modifier
                    .size(OrbSpec.GOLD_RING_SIZE)
                    .drawBehind {
                        drawCircle(
                            color = Tokens.AccentGold,
                            style = Stroke(width = 3.dp.toPx()),
                        )
                        drawCircle(
                            color = Tokens.AccentGold.copy(alpha = 0.30f),
                            radius = size.width / 2 + 5.dp.toPx(),
                            style = Stroke(width = 8.dp.toPx()),
                        )
                    },
            )
        }
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
private fun Halo(transition: InfiniteTransition, delayMs: Int) {
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
                    style = Stroke(width = 2.dp.toPx()),
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
        verticalArrangement = Arrangement.Center,
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
