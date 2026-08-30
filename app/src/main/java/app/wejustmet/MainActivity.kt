package app.wejustmet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.wejustmet.core.ContactDraft
import app.wejustmet.data.ConvexRepo
import app.wejustmet.send.WhatsAppSender
import app.wejustmet.ui.Tokens
import app.wejustmet.ui.screens.CaptureScreen
import app.wejustmet.ui.screens.HomeScreen
import app.wejustmet.ui.screens.ReviewScreen
import app.wejustmet.ui.screens.SendScreen
import app.wejustmet.ui.screens.SuccessScreen
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object Home : Screen
    data object Capture : Screen
    data class Review(val draft: ContactDraft) : Screen
    data class Send(val draft: ContactDraft) : Screen
    data object Success : Screen
}

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
                Surface(modifier = Modifier.fillMaxSize(), color = Tokens.BgPrimary) {
                    JustMetApp()
                }
            }
        }
    }
}

@Composable
private fun JustMetApp() {
    val context = LocalContext.current
    val repo = remember { ConvexRepo() }
    val scope = rememberCoroutineScope()

    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    var sending by remember { mutableStateOf(false) }
    var quoteIndex by remember { mutableIntStateOf(0) }
    val contacts by remember { repo.contacts() }.collectAsState(initial = null)

    BackHandler(enabled = screen != Screen.Home) {
        screen = when (val current = screen) {
            is Screen.Capture -> Screen.Home
            is Screen.Review -> Screen.Capture
            is Screen.Send -> Screen.Review(current.draft)
            else -> Screen.Home
        }
    }

    when (val current = screen) {
        Screen.Home -> HomeScreen(contacts = contacts, onCapture = { screen = Screen.Capture })

        Screen.Capture -> CaptureScreen(onCaptured = { screen = Screen.Review(it) })

        is Screen.Review -> ReviewScreen(
            initial = current.draft,
            onRetakeSelfie = { screen = Screen.Capture },
            onCompose = { screen = Screen.Send(it) },
        )

        is Screen.Send -> SendScreen(
            draft = current.draft,
            sending = sending,
            onSend = { message ->
                if (sending) return@SendScreen
                sending = true
                val selfie = WhatsAppSender.stagedDemoSelfie(context)
                val error = WhatsAppSender.send(context, current.draft.phone, message, selfie)
                if (error != null) {
                    sending = false
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    return@SendScreen
                }
                scope.launch {
                    runCatching { repo.saveContact(current.draft, selfie) }
                        .onFailure { failure ->
                            Toast.makeText(context, failure.message, Toast.LENGTH_LONG).show()
                        }
                    sending = false
                    screen = Screen.Success
                }
            },
        )

        Screen.Success -> SuccessScreen(
            quoteIndex = quoteIndex,
            onDone = {
                quoteIndex += 1
                screen = Screen.Home
            },
        )
    }
}
