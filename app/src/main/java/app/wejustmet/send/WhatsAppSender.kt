package app.wejustmet.send

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import app.wejustmet.R
import java.io.File

object WhatsAppSender {

    /**
     * Fires ACTION_SEND into WhatsApp with the image staged in the chat for [e164Number]
     * via the jid extra (proven on-device, PLAN step 1 verdict).
     * Returns an error message, or null on success.
     */
    fun send(context: Context, e164Number: String, message: String, image: File): String? {
        val uri = providerUri(context, image)
        val target = installedWhatsAppPackage(context)
            ?: return context.getString(R.string.error_whatsapp_missing)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = SendConfig.mimeFor(image.name)
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage(target)
            putExtra(SendConfig.WHATSAPP_JID_EXTRA, SendConfig.jidFor(e164Number))
        }
        return try {
            context.startActivity(intent)
            null
        } catch (_: ActivityNotFoundException) {
            context.getString(R.string.error_whatsapp_missing)
        }
    }

    /** Camera target inside the FileProvider-served cache dir. */
    fun selfieFile(context: Context): File =
        File(
            File(context.cacheDir, SendConfig.SHARED_CACHE_DIR).apply { mkdirs() },
            SendConfig.SELFIE_FILE_NAME,
        )

    fun selfieUri(context: Context): Uri = providerUri(context, selfieFile(context))

    /** Fallback when no selfie was captured: the bundled placeholder staged into cache. */
    fun stagedDemoSelfie(context: Context): File {
        val dir = File(context.cacheDir, SendConfig.SHARED_CACHE_DIR).apply { mkdirs() }
        val file = File(dir, SendConfig.TEST_IMAGE_ASSET)
        context.assets.open(SendConfig.TEST_IMAGE_ASSET).use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        return file
    }

    private fun providerUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(
            context,
            context.packageName + SendConfig.FILE_PROVIDER_AUTHORITY_SUFFIX,
            file,
        )

    /** First installed package from the sender priority list, or null. */
    private fun installedWhatsAppPackage(context: Context): String? =
        SendConfig.SENDER_PACKAGE_PRIORITY.firstOrNull { pkg ->
            runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess
        }
}
