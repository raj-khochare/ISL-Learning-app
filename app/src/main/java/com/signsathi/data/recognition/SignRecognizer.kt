package com.signsathi.data.recognition

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import timber.log.Timber
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

data class RecognitionResult(
    val label      : String,
    val confidence : Float,
    val category   : String
)

@Singleton
class SignRecognizer @Inject constructor(
    private val context      : Context,
    private val modelManager : ModelManager
) {
    private var handLandmarker : HandLandmarker? = null
    private var interpreter    : Interpreter?    = null
    private var labels         : List<String>    = emptyList()
    private var categoryMap    : Map<String, String> = emptyMap()

    // Predictions below this threshold are ignored — shows "no sign detected"
    private val CONFIDENCE_THRESHOLD = 0.75f

    fun initialize() {
        try {
            setupMediaPipe()
            setupTfLite()
            Timber.d("SignRecognizer: initialized with ${labels.size} signs")
        } catch (e: Exception) {
            Timber.e(e, "SignRecognizer: initialization failed")
        }
    }

    private fun setupMediaPipe() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()

            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(1)
                .setMinHandDetectionConfidence(0.6f)
                .setMinTrackingConfidence(0.6f)
                .build()

            handLandmarker = HandLandmarker.createFromOptions(context, options)
            Timber.d("SignRecognizer: MediaPipe initialized")
        } catch (e: Exception) {
            Timber.e(e, "SignRecognizer: MediaPipe not available on this device/emulator")
            handLandmarker = null
            // recognize() will return null safely — no crash
        }
    }

    private fun setupTfLite() {
        // Load TFLite model — prefers OTA-downloaded model over bundled asset
        val modelBytes = modelManager.getModelBytes()
        val modelBuffer = ByteBuffer.allocateDirect(modelBytes.size)
            .order(ByteOrder.nativeOrder())
        modelBuffer.put(modelBytes)
        modelBuffer.rewind()
        interpreter = Interpreter(modelBuffer)

        // Load label map
        val labelJson   = modelManager.getLabelMap()
        val labelsArray = labelJson.getJSONArray("labels")
        labels = (0 until labelsArray.length()).map { labelsArray.getString(it) }

        val catMap = labelJson.getJSONObject("category_map")
        categoryMap = labels.associateWith { label ->
            if (catMap.has(label)) catMap.getString(label) else "unknown"
        }
    }

    /**
     * Process a single bitmap frame and return the recognized sign.
     * Returns null if no hand is detected or confidence is below threshold.
     *
     * Call this from a background thread — it is NOT main-thread safe.
     */
    fun recognize(bitmap: Bitmap): RecognitionResult? {
        val landmarker = handLandmarker ?: return null
        val interp     = interpreter    ?: return null

        return try {
            // Step 1 — Run MediaPipe hand landmark detection
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result  = landmarker.detect(mpImage)

            if (result.landmarks().isEmpty()) return null

            // Step 2 — Extract and normalize landmarks relative to wrist
            val landmarks = result.landmarks()[0]
            val wristX    = landmarks[0].x()
            val wristY    = landmarks[0].y()
            val wristZ    = landmarks[0].z()

            val inputArray = FloatArray(63)
            landmarks.forEachIndexed { i, lm ->
                inputArray[i * 3]     = lm.x() - wristX
                inputArray[i * 3 + 1] = lm.y() - wristY
                inputArray[i * 3 + 2] = lm.z() - wristZ
            }

            // Step 3 — Run TFLite classifier
            val inputBuffer = ByteBuffer.allocateDirect(63 * 4)
                .order(ByteOrder.nativeOrder())
            inputArray.forEach { inputBuffer.putFloat(it) }

            val outputBuffer = Array(1) { FloatArray(labels.size) }
            interp.run(inputBuffer, outputBuffer)

            // Step 4 — Pick highest confidence prediction
            val probabilities = outputBuffer[0]
            val maxIndex      = probabilities.indices
                .maxByOrNull { probabilities[it] } ?: return null
            val confidence    = probabilities[maxIndex]

            if (confidence < CONFIDENCE_THRESHOLD) return null

            val label    = labels[maxIndex]
            val category = categoryMap[label] ?: "unknown"

            RecognitionResult(
                label      = label,
                confidence = confidence,
                category   = category
            )

        } catch (e: Exception) {
            Timber.e(e, "SignRecognizer: recognition failed")
            null
        }
    }

    fun isInitialized(): Boolean =
        handLandmarker != null && interpreter != null && labels.isNotEmpty()

    fun close() {
        handLandmarker?.close()
        interpreter?.close()
        handLandmarker = null
        interpreter    = null
    }
}