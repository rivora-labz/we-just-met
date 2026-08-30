package app.wejustmet.core

/** The app owner's own details, collected once at onboarding (screen 0). */
data class OwnerProfile(
    val name: String = "",
    val whatsappNumber: String = "",
    val linkedinUrl: String = "",
    /** Optional free-text: job, what I do, what I like. Rides in the composed message. */
    val about: String = "",
    /** Optional extra link (Instagram or anything else). Rides in the composed message. */
    val instagramUrl: String = "",
) {
    val isComplete: Boolean
        get() = name.isNotBlank()
}
