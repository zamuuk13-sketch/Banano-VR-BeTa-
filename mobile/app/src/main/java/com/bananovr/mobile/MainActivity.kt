package com.bananovr.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var statusText: TextView
    private lateinit var cameraButton: Button

    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                statusText.text = "Câmera bloqueada\nAutorize o acesso para continuar."
                cameraButton.text = "PERMITIR CÂMERA"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.cameraPreview)
        statusText = findViewById(R.id.statusText)
        cameraButton = findViewById(R.id.cameraButton)

        cameraButton.setOnClickListener {
            if (hasCameraPermission()) {
                lensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }

                startCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        if (hasCameraPermission()) {
            startCamera()
        } else {
            statusText.text = "Câmera necessária"
            cameraButton.text = "PERMITIR CÂMERA"
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            val cameraProvider = providerFuture.get()

            val selector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            if (!cameraProvider.hasCamera(selector)) {
                statusText.text = "Esta câmera não está disponível neste celular."
                return@addListener
            }

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, selector, preview)

            statusText.text =
                if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    "Câmera traseira ativa"
                } else {
                    "Câmera frontal ativa"
                }

            cameraButton.text = "TROCAR CÂMERA"
        }, ContextCompat.getMainExecutor(this))
    }
}
