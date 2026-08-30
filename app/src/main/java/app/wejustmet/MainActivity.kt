package app.wejustmet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wejustmet.send.SendConfig
import app.wejustmet.send.WhatsAppSender
import app.wejustmet.ui.Tokens

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Tokens.BrandPrimary,
                    background = Tokens.BgPrimary,
                    surface = Tokens.SurfaceCard,
                    error = Tokens.StatusError,
                ),
            ) {
                SendSeamScreen()
            }
        }
    }
}

@Composable
private fun SendSeamScreen() {
    val context = LocalContext.current
    var fallbackNoJid by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Tokens.BgPrimary) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                color = Tokens.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.send_seam_title),
                color = Tokens.TextSecondary,
                fontSize = 16.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.send_seam_subtitle),
                color = Tokens.TextMuted,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.target_number_label,
                    SendConfig.TEST_WHATSAPP_NUMBER,
                ),
                color = Tokens.AccentGoldText,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.fallback_toggle_label),
                    color = Tokens.TextPrimary,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = fallbackNoJid,
                    onCheckedChange = { fallbackNoJid = it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Tokens.BrandPrimary,
                        uncheckedTrackColor = Tokens.SurfaceInput,
                        uncheckedBorderColor = Tokens.BorderControl,
                    ),
                )
            }
            Spacer(Modifier.height(24.dp))
            PrimaryGreenButton(
                label = stringResource(
                    if (fallbackNoJid) R.string.cta_send_share_sheet else R.string.cta_send_jid,
                ),
                onClick = {
                    WhatsAppSender.send(context, withJid = !fallbackNoJid)?.let { error ->
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    }
                },
            )
        }
    }
}

@Composable
private fun PrimaryGreenButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Tokens.BrandPrimaryHover, Tokens.BrandPrimary, Tokens.BrandGreen),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
