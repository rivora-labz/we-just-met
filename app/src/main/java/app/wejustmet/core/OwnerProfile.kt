package app.wejustmet.core

/** The app owner's own details, collected once at onboarding (screen 0). */
data class OwnerProfile(
    val name: String = "",
    val whatsappNumber: String = "",
    val linkedinUrl: String = "",
) {
    val isComplete: Boolean
        get() = name.isNotBlank()
}
