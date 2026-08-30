package app.wejustmet.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Button spec per PLAN design language (ported from Snook PrimaryGreenButton + pressScale). */
object ButtonSpec {
    const val PRESSED_SCALE = 0.952f
    const val FIRE_DELAY_MS = 220L
    val HEIGHT = 52.dp
    val CORNER = 12.dp
    val SPINNER_SIZE = 24.dp
    val LABEL_SIZE = 16.sp
}

@Composable
fun PrimaryGreenButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) ButtonSpec.PRESSED_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pressScale",
    )
    val scope = rememberCoroutineScope()
    var firing by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(ButtonSpec.CORNER)
    val active = enabled && !loading
    val background = if (active || loading) {
        Modifier.background(
            Brush.verticalGradient(
                listOf(Tokens.BrandPrimaryHover, Tokens.BrandPrimary, Tokens.BrandGreen),
            ),
        )
    } else {
        Modifier
            .background(Tokens.SurfaceCard)
            .border(1.dp, Tokens.BorderControl, shape)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ButtonSpec.HEIGHT)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(background)
            .clickable(interactionSource = interaction, indication = null, enabled = active) {
                if (firing) return@clickable
                firing = true
                scope.launch {
                    delay(ButtonSpec.FIRE_DELAY_MS)
                    onClick()
                    firing = false
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonSpec.SPINNER_SIZE),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = label,
                color = if (active) Color.White else Tokens.TextMuted,
                fontSize = ButtonSpec.LABEL_SIZE,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
