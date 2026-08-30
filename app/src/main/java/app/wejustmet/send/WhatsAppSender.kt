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

    /** Fires the ACTION_SEND intent. Returns an error message, or null on success. */
    fun send(context: Context, withJid: Boolean): String? {
        val uri = stageTestImage(context)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = SendConfig.IMAGE_MIME
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, SendConfig.TEST_MESSAGE)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return try {
            if (withJid) {
                intent.setPackage(SendConfig.WHATSAPP_PACKAGE)
                intent.putExtra(
                    SendConfig.WHATSAPP_JID_EXTRA,
                    SendConfig.jidFor(SendConfig.TEST_WHATSAPP_NUMBER),
                )
                context.startActivity(intent)
            } else {
                context.startActivity(
                    Intent.createChooser(intent, context.getString(R.string.share_chooser_title)),
                )
            }
            null
        } catch (_: ActivityNotFoundException) {
            context.getString(R.string.error_whatsapp_missing)
        }
    }

    /** Copies the bundled test image into the FileProvider-served cache dir. */
    private fun stageTestImage(context: Context): Uri {
        val dir = File(context.cacheDir, SendConfig.SHARED_CACHE_DIR).apply { mkdirs() }
        val file = File(dir, SendConfig.TEST_IMAGE_ASSET)
        context.assets.open(SendConfig.TEST_IMAGE_ASSET).use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        return FileProvider.getUriForFile(
            context,
            context.packageName + SendConfig.FILE_PROVIDER_AUTHORITY_SUFFIX,
            file,
        )
    }
}
