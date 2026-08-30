package app.wejustmet.core

/** In-flight contact between Capture and Send. Screen state only; Convex row is the record. */
data class ContactDraft(
    val name: String = "",
    val phone: String = "",
    val company: String = "",
    val role: String = "",
    val note: String = "",
) {
    val readyToCompose: Boolean
        get() = name.isNotBlank() && phone.isNotBlank()
}

/**
 * Step-4 hardcoded capture result (PLAN: screens with hardcoded data; STT lands in step 5).
 * Matches the scripted demo persona from the orb preview.
 */
val DEMO_CAPTURE_RESULT = ContactDraft(
    name = "Sarah Chen",
    phone = "+971555524418",
    company = "Falcon Ventures",
    role = "Partnerships",
    note = "the Dubai padel league",
)
