package app.wejustmet.core

/**
 * Client-side E.164 normalization, mirrors web/convex/extract.ts normalizePhone.
 * Manually edited numbers (e.g. "058 591 4562") must normalize before the WhatsApp
 * jid is built: a jid with a leading zero is invalid and WhatsApp silently falls
 * back to its forward picker.
 */
object PhoneNormalizer {
    fun toE164(raw: String): String {
        val hasPlus = raw.trim().startsWith("+")
        val digits = raw.filter(Char::isDigit)
        if (digits.isEmpty()) return ""
        val countryDigits = AppConfig.DEFAULT_COUNTRY_CODE.removePrefix("+")
        return when {
            hasPlus -> "+$digits"
            digits.startsWith("00") -> "+${digits.drop(2)}"
            digits.startsWith(countryDigits) && digits.length >= 11 -> "+$digits"
            digits.startsWith("0") -> "+$countryDigits${digits.drop(1)}"
            else -> "+$countryDigits$digits"
        }
    }
}
