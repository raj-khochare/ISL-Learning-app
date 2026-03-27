package com.signsathi.data.recognition

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraManager @Inject constructor(
    private val context: Context
) {
    private var cameraExecutor  : ExecutorService?      = null
    private var cameraProvider  : ProcessCameraProvider? = null

    /**
     * Binds the camera to the given lifecycle and PreviewView.
     * Delivers each frame as a Bitmap to [onFrameAvailable].
     *
     * @param lifecycleOwner  The Activity or Fragment lifecycle
     * @param previewView     The composable PreviewView surface
     * @param onFrameAvailable  Called on a background thread for each frame
     */
    fun startCamera(
        lifecycleOwner   : LifecycleOwner,
        previewView      : PreviewView,
        onFrameAvailable : (Bitmap) -> Unit
    ) {
        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor!!) { imageProxy ->
                val bitmap = imageProxy.toBitmap()
                onFrameAvailable(bitmap)
                imageProxy.close()
            }

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )
                Timber.d("CameraManager: camera started")
            } catch (e: Exception) {
                Timber.e(e, "CameraManager: failed to bind camera")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        cameraExecutor?.shutdown()
        cameraExecutor = null
        Timber.d("CameraManager: camera stopped")
    }
}