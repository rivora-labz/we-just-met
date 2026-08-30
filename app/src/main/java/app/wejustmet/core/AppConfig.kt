package app.wejustmet.core

/**
 * Product config, single source of truth (PLAN: names + owner profile fallback constants,
 * cut line 3: first-run setup screen replaced by this config object).
 * Zero Android imports in core/.
 */
object AppConfig {
    const val PRODUCT_NAME = "We Just Met"

    /** Owner profile fallback (screen 0 skipped per cut line 3). */
    const val OWNER_NAME = "Narayan"
    const val OWNER_LINKEDIN_URL = ""

    /** Default country code used when a spoken number has no prefix (PLAN step 5). */
    const val DEFAULT_COUNTRY_CODE = "+971"

    /** Success screen (4b) rotating headlines, never inline literals. */
    val SUCCESS_QUOTES = listOf(
        "Now they can't forget you.",
        "See you on the other side.",
        "The follow-up is already working.",
    )
    const val SUCCESS_SUBLINE = "Saved. Nudge in 48h."
    const val SUCCESS_AUTO_DISMISS_MS = 2500L

    const val HOME_EMPTY_STATE = "Meet someone. Hand them your phone."
    const val CAPTURE_PROMPT_TITLE = "Hi! I'm listening."
    const val CAPTURE_PROMPT_LINE = "Say your name, number, company and what we talked about."
    const val CAPTURE_HINT = "tap the orb to stop"
    const val COUNTDOWN_READY_LINE = "Ready for selfie"
    const val COUNTDOWN_SUB_LINE = "extraction already running in the background"
    const val COUNTDOWN_BEAT_MS = 800L
    val COUNTDOWN_STEPS = listOf("3", "2", "1", "GO")
}
