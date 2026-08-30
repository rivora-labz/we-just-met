package app.wejustmet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wejustmet.R
import app.wejustmet.core.AppConfig
import app.wejustmet.data.ContactRow
import app.wejustmet.ui.PrimaryGreenButton
import app.wejustmet.ui.Tokens

private const val MINUTE_MS = 60_000.0
private const val HOUR_MS = 60 * MINUTE_MS
private const val DAY_MS = 24 * HOUR_MS

private fun metAgo(metAt: Double, now: Long): String {
    val delta = (now - metAt).coerceAtLeast(0.0)
    return when {
        delta < HOUR_MS -> "met ${maxOf(1, (delta / MINUTE_MS).toInt())}m ago"
        delta < DAY_MS -> "met ${(delta / HOUR_MS).toInt()}h ago"
        else -> "met ${(delta / DAY_MS).toInt()}d ago"
    }
}

@Composable
fun HomeScreen(contacts: List<ContactRow>?, onCapture: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = AppConfig.PRODUCT_NAME,
            color = Tokens.BrandGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (contacts.isNullOrEmpty()) {
                Text(
                    text = AppConfig.HOME_EMPTY_STATE,
                    color = Tokens.TextMuted,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                val now = System.currentTimeMillis()
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(contacts, key = { it._id }) { contact ->
                        ContactCard(contact, now)
                    }
                }
            }
        }

        PrimaryGreenButton(label = stringResource(R.string.cta_we_just_met), onClick = onCapture)
    }
}

@Composable
private fun ContactCard(contact: ContactRow, now: Long) {
    Surface(
        color = Tokens.SurfaceCard,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(Tokens.BrandPrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = contact.name.take(1),
                    color = Tokens.SurfaceCard,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    color = Tokens.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = listOfNotNull(contact.role, contact.company)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                        .ifBlank { contact.phone },
                    color = Tokens.TextSecondary,
                    fontSize = 13.sp,
                )
                Text(
                    text = metAgo(contact.metAt, now),
                    color = Tokens.TextMuted,
                    fontSize = 12.sp,
                )
            }
            if (contact.enrichment == ENRICHMENT_DONE) {
                Text(
                    text = stringResource(R.string.badge_enriched),
                    color = Tokens.AccentGoldText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** Mirrors web/convex/shared.ts ENRICHMENT.done; the wire value crosses the Convex boundary. */
private const val ENRICHMENT_DONE = "done"
