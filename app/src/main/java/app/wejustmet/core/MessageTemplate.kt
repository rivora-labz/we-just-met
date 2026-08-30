package app.wejustmet.core

/** Outgoing WhatsApp message. Owner fields omitted gracefully when blank. */
object MessageTemplate {
    fun compose(draft: ContactDraft, ownerName: String, ownerLinkedInUrl: String): String {
        val firstName = draft.name.trim().substringBefore(' ')
        val lines = buildList {
            add("Hi $firstName, great meeting you! It's $ownerName, we just met.")
            if (draft.note.isNotBlank()) add("Loved talking about ${draft.note.trim()}.")
            if (ownerLinkedInUrl.isNotBlank()) add("Here's my LinkedIn so we stay connected: $ownerLinkedInUrl")
        }
        return lines.joinToString("\n\n")
    }
}
