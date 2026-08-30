package app.wejustmet.ui.screens

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wejustmet.R
import app.wejustmet.core.OwnerProfile
import app.wejustmet.ui.PrimaryGreenButton
import app.wejustmet.ui.Tokens

/** Screen 0, one-time: "This is you." Feeds the outgoing message template. */
@Composable
fun OnboardingScreen(initial: OwnerProfile, onSave: (OwnerProfile) -> Unit) {
    var profile by remember { mutableStateOf(initial) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            color = Tokens.SurfaceCard,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_title),
                    color = Tokens.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.onboarding_subtitle),
                    color = Tokens.TextMuted,
                    fontSize = 14.sp,
                )
                OwnerField(stringResource(R.string.field_your_name), profile.name) {
                    profile = profile.copy(name = it)
                }
                OwnerField(stringResource(R.string.field_your_whatsapp), profile.whatsappNumber) {
                    profile = profile.copy(whatsappNumber = it)
                }
                OwnerField(stringResource(R.string.field_your_linkedin), profile.linkedinUrl) {
                    profile = profile.copy(linkedinUrl = it)
                }
                OwnerField(stringResource(R.string.field_your_instagram), profile.instagramUrl) {
                    profile = profile.copy(instagramUrl = it)
                }
                OwnerField(
                    label = stringResource(R.string.field_your_about),
                    value = profile.about,
                    singleLine = false,
                ) {
                    profile = profile.copy(about = it)
                }
                Spacer(Modifier.height(4.dp))
                PrimaryGreenButton(
                    label = stringResource(R.string.cta_onboarding_save),
                    enabled = profile.isComplete,
                    onClick = { onSave(profile) },
                )
            }
        }
    }
}

@Composable
private fun OwnerField(
    label: String,
    value: String,
    singleLine: Boolean = true,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
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
