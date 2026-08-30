package app.wejustmet.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wejustmet.core.AppConfig
import app.wejustmet.ui.Tokens
import kotlinx.coroutines.delay

private const val CHECKMARK = "\u2713"

@Composable
fun SuccessScreen(quoteIndex: Int, onDone: () -> Unit) {
    var shown by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "burst",
    )

    LaunchedEffect(Unit) {
        shown = true
        delay(AppConfig.SUCCESS_AUTO_DISMISS_MS)
        onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Tokens.BgPrimary)
            .clickable(onClick = onDone)
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(Tokens.BrandPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = CHECKMARK,
                color = Tokens.AccentGold,
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = AppConfig.SUCCESS_QUOTES[quoteIndex % AppConfig.SUCCESS_QUOTES.size],
            color = Tokens.BrandGreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = AppConfig.SUCCESS_SUBLINE,
            color = Tokens.TextMuted,
            fontSize = 15.sp,
        )
    }
}
