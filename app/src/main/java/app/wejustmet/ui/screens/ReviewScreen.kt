package app.wejustmet.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wejustmet.R
import app.wejustmet.core.ContactDraft
import app.wejustmet.core.MessageTemplate
import app.wejustmet.core.OwnerProfile
import app.wejustmet.send.SendConfig
import app.wejustmet.ui.PrimaryGreenButton
import app.wejustmet.ui.Tokens
import java.io.File

@Composable
fun ReviewScreen(
    initial: ContactDraft,
    owner: OwnerProfile,
    selfie: File?,
    sending: Boolean,
    onRetakeSelfie: () -> Unit,
    onSend: (draft: ContactDraft, message: String) -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }
    var messageEdited by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    // Template tracks live field edits until the user touches the message itself.
    val templated = MessageTemplate.compose(draft, owner)
    val shownMessage = if (messageEdited) message else templated
    val context = LocalContext.current
    val selfieBitmap = remember(selfie) {
        val captured = selfie?.takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.absolutePath) }
        (captured ?: context.assets.open(SendConfig.TEST_IMAGE_ASSET).use(BitmapFactory::decodeStream))
            .asImageBitmap()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.review_title),
            color = Tokens.TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Image(
            bitmap = selfieBitmap,
            contentDescription = stringResource(R.string.review_selfie_description),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .clickable(onClick = onRetakeSelfie),
        )
        Text(
            text = stringResource(R.string.review_retake_hint),
            color = Tokens.TextMuted,
            fontSize = 12.sp,
        )

        ReviewField(stringResource(R.string.field_name), draft.name) { draft = draft.copy(name = it) }
        ReviewField(stringResource(R.string.field_phone), draft.phone) { draft = draft.copy(phone = it) }
        ReviewField(stringResource(R.string.field_company), draft.company) { draft = draft.copy(company = it) }
        ReviewField(stringResource(R.string.field_role), draft.role) { draft = draft.copy(role = it) }
        ReviewField(stringResource(R.string.field_note), draft.note) { draft = draft.copy(note = it) }

        OutlinedTextField(
            value = shownMessage,
            onValueChange = {
                messageEdited = true
                message = it
            },
            label = { Text(stringResource(R.string.field_message)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Tokens.BrandPrimary,
                unfocusedBorderColor = Tokens.BorderControl,
                focusedLabelColor = Tokens.BrandPrimary,
                unfocusedContainerColor = Tokens.SurfaceInput,
                focusedContainerColor = Tokens.SurfaceCard,
            ),
        )

        Spacer(Modifier.height(8.dp))
        PrimaryGreenButton(
            label = stringResource(R.string.cta_send_whatsapp),
            enabled = draft.readyToCompose,
            loading = sending,
            onClick = { onSend(draft, shownMessage) },
        )
    }
}

@Composable
private fun ReviewField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Tokens.BrandPrimary,
            unfocusedBorderColor = Tokens.BorderControl,
            focusedLabelColor = Tokens.BrandPrimary,
            unfocusedContainerColor = Tokens.SurfaceInput,
            focusedContainerColor = Tokens.SurfaceCard,
        ),
    )
}
