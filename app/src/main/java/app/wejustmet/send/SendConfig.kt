package app.wejustmet.send

/**
 * WhatsApp send seam configuration, single source of truth.
 * jid direct-to-thread proven on-device (PLAN step 1 verdict).
 */
object SendConfig {
    const val WHATSAPP_PACKAGE = "com.whatsapp"
    const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

    /** jid path targets the first of these that is installed on the device. */
    val SENDER_PACKAGE_PRIORITY = listOf(WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE)

    const val WHATSAPP_JID_EXTRA = "jid"
    const val WHATSAPP_JID_SUFFIX = "@s.whatsapp.net"

    const val TEST_IMAGE_ASSET = "just_met_test.png"
    const val IMAGE_MIME = "image/png"

    /** Must match the cache-path entry in res/xml/file_paths.xml. */
    const val SHARED_CACHE_DIR = "shared"
    const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"

    /** WhatsApp jid for an individual chat: digits only, no plus sign. */
    fun jidFor(e164Number: String): String =
        e164Number.filter(Char::isDigit) + WHATSAPP_JID_SUFFIX
}
