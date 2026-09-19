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
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity(), HandLandmarkerHelper.Listener, HeadTrackingManager.Listener {
    private lateinit var previewView:PreviewView
    private lateinit var overlay:HandLandmarkOverlay
    private lateinit var labView:TrackingLabView
    private lateinit var sensor3DView:Sensor3DView
    private lateinit var statusText:TextView
    private lateinit var cameraButton:Button
    private lateinit var recenterButton:Button
    private lateinit var sensor3DButton:Button
    private lateinit var vrButton:Button
    private lateinit var vrView:VRView
    private lateinit var connectButton:Button
    private var transport:TrackingTransport?=null
    private var pcIp="192.168.0.100"
    private lateinit var cameraExecutor:ExecutorService
    private lateinit var handLandmarker:HandLandmarkerHelper
    private lateinit var handProcessor:HandTrackingProcessor
    private lateinit var headTrackingManager:HeadTrackingManager
    private val analyzing=AtomicBoolean(false)
    private var lensFacing=CameraSelector.LENS_FACING_BACK
    private var latestHands:HandTrackingState?=null

    private val permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->
        if(granted)startCamera() else {
            statusText.text="Câmera bloqueada\nAutorize o acesso para usar o tracking."
            cameraButton.text="PERMITIR CÂMERA"
        }
    }

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView=findViewById(R.id.cameraPreview)
        overlay=findViewById(R.id.handOverlay)
        labView=findViewById(R.id.trackingLab)
        sensor3DView=findViewById(R.id.sensor3DView)
        statusText=findViewById(R.id.statusText)
        cameraButton=findViewById(R.id.cameraButton)
        recenterButton=findViewById(R.id.recenterButton)
        sensor3DButton=findViewById(R.id.sensor3DButton)
        vrButton=findViewById(R.id.vrButton)
        vrView=findViewById(R.id.vrView)
        connectButton=findViewById(R.id.connectButton)
        transport=TrackingTransport(this)
        connectButton.setOnClickListener{if(transport?.connect(pcIp)==true)connectButton.text="PC: CONECTADO" else connectButton.text="PC: FALHA"}
        cameraExecutor=Executors.newSingleThreadExecutor()
        handLandmarker=HandLandmarkerHelper(this,this)
        handProcessor=HandTrackingProcessor()
        headTrackingManager=HeadTrackingManager(this,this)
        recenterButton.setOnClickListener{headTrackingManager.recenter()}
        sensor3DButton.setOnClickListener{sensor3DView.visibility=android.view.View.VISIBLE;vrView.visibility=android.view.View.GONE;labView.visibility=android.view.View.GONE;sensor3DView.start()}
        vrButton.setOnClickListener{vrView.visibility=android.view.View.VISIBLE;sensor3DView.visibility=android.view.View.GONE;labView.visibility=android.view.View.GONE;sensor3DView.stop()}
        cameraButton.setOnClickListener{
            if(hasCameraPermission()){
                lensFacing=if(lensFacing==CameraSelector.LENS_FACING_BACK)CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                startCamera()
            }else permissionLauncher.launch(Manifest.permission.CAMERA)
        }
        if(hasCameraPermission())startCamera() else {
            statusText.text="Câmera necessária";cameraButton.text="PERMITIR CÂMERA"
        }
    }

    override fun onResume(){super.onResume();headTrackingManager.start();sensor3DView.start()}
    override fun onPause(){sensor3DView.stop();headTrackingManager.stop();super.onPause()}

    private fun hasCameraPermission()=ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED

    private fun startCamera(){
        val future=ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider=future.get()
            val selector=CameraSelector.Builder().requireLensFacing(lensFacing).build()
            if(!provider.hasCamera(selector)){statusText.text="Esta câmera não está disponível neste celular.";return@addListener}
            val preview=Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val analysis=ImageAnalysis.Builder()
                .setTargetResolution(Size(640,480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888).build()
            analysis.setAnalyzer(cameraExecutor){imageProxy->
                if(!analyzing.compareAndSet(false,true)){imageProxy.close();return@setAnalyzer}
                try{handLandmarker.detectAsync(imageProxy.toBitmap(),imageProxy.imageInfo.timestamp/1_000_000L)}
                catch(_:Exception){analyzing.set(false)}
                finally{imageProxy.close()}
            }
            provider.unbindAll()
            provider.bindToLifecycle(this,selector,preview,analysis)
            statusText.text=if(lensFacing==CameraSelector.LENS_FACING_BACK)"Tracking ativo • câmera traseira" else "Tracking ativo • câmera frontal"
            cameraButton.text="TROCAR CÂMERA"
        },ContextCompat.getMainExecutor(this))
    }

    override fun onResult(result:HandLandmarkerResult,inferenceTimeMs:Long){
        analyzing.set(false)
        val tracking=handProcessor.process(result,System.nanoTime())
        latestHands=tracking
        runOnUiThread{
            overlay.setState(tracking)
            labView.update(latestHands,headTrackingManager.currentState())
            val h=headTrackingManager.currentState()
            statusText.text=String.format(Locale.US,
                "Mãos: %d • Cabeça: %s • %.0f Hz\nY %.0f° P %.0f° R %.0f° • Pos %.2f %.2f %.2f",
                tracking.hands.size,if(h.available)"OK" else "OFF",h.updateRateHz,
                h.yawDegrees,h.pitchDegrees,h.rollDegrees,h.position.x,h.position.y,h.position.z)
        }
    }

    override fun onError(message:String){analyzing.set(false);runOnUiThread{statusText.text="Tracking: $message"}}
    override fun onHeadTrackingState(state:HeadTrackingState){runOnUiThread{labView.update(latestHands,state);sensor3DView.update(state);vrView.update(state)}; transport?.send(BananoVRPacket(System.nanoTime(),state,latestHands))}

    override fun onDestroy(){
        transport?.close();handLandmarker.close();headTrackingManager.close();cameraExecutor.shutdown();super.onDestroy()
    }

    private fun ImageProxy.toBitmap():Bitmap{
        val buffer=planes[0].buffer
        buffer.rewind()
        val bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }
}
