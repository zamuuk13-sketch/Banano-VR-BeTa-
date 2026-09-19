package com.bananovr.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity(), HandLandmarkerHelper.Listener {
    private lateinit var previewView: PreviewView
    private lateinit var overlay: HandLandmarkOverlay
    private lateinit var statusText: TextView
    private lateinit var cameraButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var handLandmarker: HandLandmarkerHelper

    private val analyzing = AtomicBoolean(false)
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                statusText.text = "Câmera bloqueada\nAutorize o acesso para usar o tracking."
                cameraButton.text = "PERMITIR CÂMERA"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.cameraPreview)
        overlay = findViewById(R.id.handOverlay)
        statusText = findViewById(R.id.statusText)
        cameraButton = findViewById(R.id.cameraButton)
        cameraExecutor = Executors.newSingleThreadExecutor()
        handLandmarker = HandLandmarkerHelper(this, this)

        cameraButton.setOnClickListener {
            if (hasCameraPermission()) {
                lensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_BACK)
                        CameraSelector.LENS_FACING_FRONT
                    else CameraSelector.LENS_FACING_BACK
                startCamera()
            } else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        if (hasCameraPermission()) startCamera()
        else {
            statusText.text = "Câmera necessária"
            cameraButton.text = "PERMITIR CÂMERA"
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val cameraProvider = providerFuture.get()
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            if (!cameraProvider.hasCamera(selector)) {
                statusText.text = "Esta câmera não está disponível neste celular."
                return@addListener
            }

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val analysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(640, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                if (!analyzing.compareAndSet(false, true)) {
                    imageProxy.close()
                    return@setAnalyzer
                }
                try {
                    val bitmap = imageProxy.toBitmap()
                    handLandmarker.detectAsync(bitmap, imageProxy.imageInfo.timestamp / 1_000_000L)
                } catch (e: Exception) {
                    analyzing.set(false)
                    imageProxy.close()
                } finally {
                    imageProxy.close()
                }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, selector, preview, analysis)
            statusText.text =
                if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    "Tracking ativo • câmera traseira"
                else "Tracking ativo • câmera frontal"
            cameraButton.text = "TROCAR CÂMERA"
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onResult(result: HandLandmarkerResult, inferenceTimeMs: Long) {
        analyzing.set(false)
        runOnUiThread {
            overlay.setResult(result)
            statusText.text = if (result.landmarks().isEmpty())
                "Tracking ativo • procurando mãos"
            else "Tracking ativo • " + result.landmarks().size + " mão(s)"
        }
    }

    override fun onError(message: String) {
        analyzing.set(false)
        runOnUiThread { statusText.text = "Tracking: $message" }
    }

    override fun onDestroy() {
        handLandmarker.close()
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }
}
