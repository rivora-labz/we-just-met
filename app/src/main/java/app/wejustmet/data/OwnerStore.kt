package app.wejustmet.data

import android.content.Context
import androidx.core.content.edit
import app.wejustmet.core.AppConfig
import app.wejustmet.core.OwnerProfile

/**
 * Local persistence for the one-time onboarding profile.
 * PLAN deviation note (recorded in PLAN.md): SharedPreferences instead of DataStore,
 * synchronous first read avoids an async flash of the wrong start screen.
 */
class OwnerStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Unset fields fall back to the AppConfig owner constants (PLAN cut line 3). */
    fun load(): OwnerProfile = OwnerProfile(
        name = prefs.getString(KEY_NAME, null) ?: AppConfig.OWNER_NAME,
        whatsappNumber = prefs.getString(KEY_WHATSAPP, null) ?: "",
        linkedinUrl = prefs.getString(KEY_LINKEDIN, null) ?: AppConfig.OWNER_LINKEDIN_URL,
    )

    fun isOnboarded(): Boolean = prefs.contains(KEY_NAME)

    fun save(profile: OwnerProfile) {
        prefs.edit {
            putString(KEY_NAME, profile.name.trim())
            putString(KEY_WHATSAPP, profile.whatsappNumber.trim())
            putString(KEY_LINKEDIN, profile.linkedinUrl.trim())
        }
    }

    private companion object {
        const val PREFS_NAME = "owner_profile"
        const val KEY_NAME = "name"
        const val KEY_WHATSAPP = "whatsapp"
        const val KEY_LINKEDIN = "linkedin"
    }
}
