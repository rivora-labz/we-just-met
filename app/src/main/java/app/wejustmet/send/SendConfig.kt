package app.wejustmet.send

/**
 * Step-1 send seam configuration, single source of truth.
 * The jid extra is undocumented WhatsApp behavior; this config powers the
 * on-phone A/B test (jid direct-to-thread vs plain share sheet).
 */
object SendConfig {
    /** Founder's business WhatsApp number (E.164) for the step-1 A/B test only. */
    const val TEST_WHATSAPP_NUMBER = "+971551758694"

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

    const val TEST_MESSAGE =
        "Great meeting you! We Just Met send-seam test (step 1)."

    /** WhatsApp jid for an individual chat: digits only, no plus sign. */
    fun jidFor(e164Number: String): String =
        e164Number.filter(Char::isDigit) + WHATSAPP_JID_SUFFIX
}
