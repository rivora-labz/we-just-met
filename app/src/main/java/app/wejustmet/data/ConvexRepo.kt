package app.wejustmet.data

import app.wejustmet.BuildConfig
import app.wejustmet.core.ContactDraft
import dev.convex.android.ConvexClient
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Function names shared with web/convex; keep in sync with that module. */
private const val FN_CONTACTS_LIST = "contacts:list"
private const val FN_CONTACTS_SAVE = "contacts:save"
private const val FN_GENERATE_UPLOAD_URL = "contacts:generateUploadUrl"
private const val UPLOAD_RESPONSE_STORAGE_ID = "storageId"
private const val SELFIE_MIME = "image/png"

@Serializable
data class ContactRow(
    val _id: String,
    val name: String,
    val phone: String,
    val company: String? = null,
    val role: String? = null,
    val note: String? = null,
    val linkedinUrl: String? = null,
    val selfieUrl: String? = null,
    val enrichment: String,
    val metAt: Double,
    val followUpAt: Double,
)

class ConvexRepo(private val client: ConvexClient = ConvexClient(BuildConfig.CONVEX_URL)) {

    fun contacts(): Flow<List<ContactRow>> =
        client.subscribe<List<ContactRow>>(FN_CONTACTS_LIST).map { it.getOrDefault(emptyList()) }

    /** Uploads the selfie (when present) then saves the contact. Returns the new row id. */
    suspend fun saveContact(draft: ContactDraft, selfie: File?): String {
        val selfieId = selfie?.let { uploadSelfie(it) }
        val args = buildMap {
            put("name", draft.name.trim())
            put("phone", draft.phone.trim())
            if (draft.company.isNotBlank()) put("company", draft.company.trim())
            if (draft.role.isNotBlank()) put("role", draft.role.trim())
            if (draft.note.isNotBlank()) put("note", draft.note.trim())
            if (selfieId != null) put("selfieId", selfieId)
        }
        return client.mutation<String>(FN_CONTACTS_SAVE, args)
    }

    private suspend fun uploadSelfie(file: File): String {
        val uploadUrl = client.mutation<String>(FN_GENERATE_UPLOAD_URL, emptyMap())
        return withContext(Dispatchers.IO) {
            val connection = URL(uploadUrl).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", SELFIE_MIME)
                connection.outputStream.use { out -> file.inputStream().use { it.copyTo(out) } }
                val body = connection.inputStream.bufferedReader().readText()
                Json.parseToJsonElement(body)
                    .jsonObject.getValue(UPLOAD_RESPONSE_STORAGE_ID)
                    .jsonPrimitive.content
            } finally {
                connection.disconnect()
            }
        }
    }
}
