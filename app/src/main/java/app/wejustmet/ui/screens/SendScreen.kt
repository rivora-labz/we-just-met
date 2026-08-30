package app.wejustmet.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wejustmet.R
import app.wejustmet.core.AppConfig
import app.wejustmet.core.ContactDraft
import app.wejustmet.core.MessageTemplate
import app.wejustmet.send.SendConfig
import app.wejustmet.ui.PrimaryGreenButton
import app.wejustmet.ui.Tokens

@Composable
fun SendScreen(draft: ContactDraft, sending: Boolean, onSend: (message: String) -> Unit) {
    var message by remember {
        mutableStateOf(
            MessageTemplate.compose(draft, AppConfig.OWNER_NAME, AppConfig.OWNER_LINKEDIN_URL),
        )
    }
    val context = LocalContext.current
    val selfie = remember {
        context.assets.open(SendConfig.TEST_IMAGE_ASSET).use {
            BitmapFactory.decodeStream(it).asImageBitmap()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.send_title, draft.name.substringBefore(' ')),
            color = Tokens.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = draft.phone,
            color = Tokens.AccentGoldText,
            fontSize = 13.sp,
        )

        // WhatsApp-style preview bubble: selfie on top, editable text below.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Tokens.SurfaceCard),
        ) {
            Image(
                bitmap = selfie,
                contentDescription = stringResource(R.string.review_selfie_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    color = Tokens.TextPrimary,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Tokens.SurfaceCard,
                    unfocusedContainerColor = Tokens.SurfaceCard,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Tokens.BrandPrimary,
                ),
            )
        }

        Spacer(Modifier.height(8.dp))
        PrimaryGreenButton(
            label = stringResource(R.string.cta_send_whatsapp),
            loading = sending,
            onClick = { onSend(message) },
        )
    }
}
