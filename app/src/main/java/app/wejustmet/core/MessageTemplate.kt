package app.wejustmet.core

/** Outgoing WhatsApp message. Owner fields omitted gracefully when blank. */
object MessageTemplate {
    fun compose(draft: ContactDraft, owner: OwnerProfile): String {
        val firstName = draft.name.trim().substringBefore(' ')
        val lines = buildList {
            add("Hi $firstName, great meeting you! It's ${owner.name}, we just met.")
            if (draft.note.isNotBlank()) add("Loved talking about ${draft.note.trim()}.")
            if (owner.about.isNotBlank()) add("A bit about me: ${owner.about.trim()}")
            if (owner.linkedinUrl.isNotBlank()) add("Here's my LinkedIn so we stay connected: ${owner.linkedinUrl}")
            if (owner.instagramUrl.isNotBlank()) add("And my Instagram: ${owner.instagramUrl}")
        }
        return lines.joinToString("\n\n")
    }
}
