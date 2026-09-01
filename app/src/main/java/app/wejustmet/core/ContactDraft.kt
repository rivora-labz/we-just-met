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

    val isEmpty: Boolean
        get() = name.isBlank() && phone.isBlank() && company.isBlank() &&
            role.isBlank() && note.isBlank()
}

