package app.wejustmet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.wejustmet.core.AppConfig
import app.wejustmet.core.ContactDraft
import app.wejustmet.data.ConvexRepo
import app.wejustmet.data.OwnerStore
import app.wejustmet.send.WhatsAppSender
import app.wejustmet.ui.Tokens
import app.wejustmet.ui.screens.CaptureScreen
import app.wejustmet.ui.screens.OnboardingScreen
import app.wejustmet.ui.screens.PeopleScreen
import app.wejustmet.ui.screens.ReviewScreen
import app.wejustmet.ui.screens.SuccessScreen
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object Onboarding : Screen
    data object Capture : Screen
    data object People : Screen
    data class Review(val draft: ContactDraft) : Screen
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
    val ownerStore = remember { OwnerStore(context) }
    val scope = rememberCoroutineScope()

    var owner by remember { mutableStateOf(ownerStore.load()) }
    var screen by remember {
        mutableStateOf<Screen>(if (ownerStore.isOnboarded()) Screen.Capture else Screen.Onboarding)
    }
    var sending by remember { mutableStateOf(false) }
    var quoteIndex by remember { mutableIntStateOf(0) }
    val contacts by remember { repo.contacts() }.collectAsState(initial = null)
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    BackHandler(enabled = screen != Screen.Capture) {
        screen = when (screen) {
            is Screen.Review -> Screen.Capture
            Screen.People -> Screen.Capture
            Screen.Onboarding -> if (owner.isComplete) Screen.Capture else Screen.Onboarding
            else -> Screen.Capture
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = screen == Screen.Capture,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Tokens.BgPrimary) {
                Text(
                    text = AppConfig.PRODUCT_NAME,
                    color = Tokens.BrandGreen,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(24.dp),
                )
                DrawerItem(stringResource(R.string.menu_people)) {
                    scope.launch { drawerState.close() }
                    screen = Screen.People
                }
                DrawerItem(stringResource(R.string.menu_your_details)) {
                    scope.launch { drawerState.close() }
                    screen = Screen.Onboarding
                }
            }
        },
    ) {
        when (val current = screen) {
            Screen.Onboarding -> OnboardingScreen(
                initial = owner,
                onSave = { profile ->
                    ownerStore.save(profile)
                    owner = profile
                    screen = Screen.Capture
                },
            )

            Screen.Capture -> CaptureScreen(
                extract = repo::extract,
                onCaptured = { screen = Screen.Review(it) },
                onMenu = { scope.launch { drawerState.open() } },
            )

            Screen.People -> PeopleScreen(
                contacts = contacts,
                onBack = { screen = Screen.Capture },
            )

            is Screen.Review -> ReviewScreen(
                initial = current.draft,
                owner = owner,
                sending = sending,
                onRetakeSelfie = { screen = Screen.Capture },
                onSend = { draft, message ->
                    if (sending) return@ReviewScreen
                    sending = true
                    val selfie = WhatsAppSender.stagedDemoSelfie(context)
                    val error = WhatsAppSender.send(context, draft.phone, message, selfie)
                    if (error != null) {
                        sending = false
                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        return@ReviewScreen
                    }
                    scope.launch {
                        runCatching { repo.saveContact(draft, selfie) }
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
                    screen = Screen.Capture
                },
            )
        }
    }
}

@Composable
private fun DrawerItem(label: String, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(text = label, color = Tokens.TextPrimary, fontSize = 15.sp) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp),
    )
}
