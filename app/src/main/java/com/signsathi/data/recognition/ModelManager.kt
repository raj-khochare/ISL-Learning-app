package com.signsathi.data.recognition

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelManager @Inject constructor(
    private val context: Context
) {
    companion object {
        // Replace with your actual S3 bucket URL after you upload the model
        private const val LATEST_URL  =
            "https://your-signsathi-bucket.s3.ap-south-1.amazonaws.com/models/latest.json"
        private const val PREFS_NAME  = "model_prefs"
        private const val KEY_VERSION = "model_version"
    }

    private val prefs      = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val modelFile  = File(context.filesDir, "sign_classifier.tflite")
    private val labelsFile = File(context.filesDir, "label_map.json")

    /**
     * Check S3 for a newer model version and download if available.
     * Silently falls back if network is unavailable.
     * Call this once on app start from a background coroutine.
     */
    suspend fun checkForUpdates(): Boolean = withContext(Dispatchers.IO) {
        try {
            val json     = URL(LATEST_URL).readText()
            val latest   = JSONObject(json)
            val version  = latest.getString("version")
            val current  = prefs.getString(KEY_VERSION, "none")

            if (version == current && modelFile.exists()) {
                Timber.d("ModelManager: already on latest version $version")
                return@withContext false
            }

            Timber.d("ModelManager: downloading model $version")

            downloadFile(latest.getString("model_url"),  modelFile)
            downloadFile(latest.getString("labels_url"), labelsFile)

            prefs.edit().putString(KEY_VERSION, version).apply()
            Timber.d("ModelManager: updated to $version")
            true

        } catch (e: Exception) {
            // Network unavailable or S3 not configured yet — use bundled assets
            Timber.d("ModelManager: OTA check skipped — ${e.message}")
            false
        }
    }

    private fun downloadFile(url: String, dest: File) {
        URL(url).openStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Returns model bytes.
     * Priority: downloaded OTA file > bundled asset
     */
    fun getModelBytes(): ByteArray =
        if (modelFile.exists()) {
            modelFile.readBytes()
        } else {
            context.assets.open("sign_classifier.tflite").readBytes()
        }

    /**
     * Returns parsed label map JSON.
     * Priority: downloaded OTA file > bundled asset
     */
    fun getLabelMap(): JSONObject {
        val json = if (labelsFile.exists()) {
            labelsFile.readText()
        } else {
            context.assets.open("label_map.json")
                .bufferedReader()
                .readText()
        }
        return JSONObject(json)
    }

    fun getCurrentVersion(): String =
        prefs.getString(KEY_VERSION, "bundled") ?: "bundled"
}