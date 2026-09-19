package com.bananovr.mobile

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkerHelper(context: Context, private val listener: Listener) : AutoCloseable {
    interface Listener {
        fun onResult(result: HandLandmarkerResult, inferenceTimeMs: Long)
        fun onError(message: String)
    }

    private val landmarker: HandLandmarker

    init {
        val base = BaseOptions.builder().setModelAssetPath(MODEL_NAME).build()
        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(base)
            .setNumHands(2)
            .setMinHandDetectionConfidence(0.5f)
            .setMinHandPresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, timestamp -> listener.onResult(result, timestamp) }
            .setErrorListener { error -> listener.onError(error.message ?: "Erro no Hand Landmarker") }
            .build()
        landmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun detectAsync(bitmap: Bitmap, timestampMs: Long) {
        try {
            landmarker.detectAsync(BitmapImageBuilder(bitmap).build(), timestampMs)
        } catch (e: Exception) {
            listener.onError(e.message ?: "Falha ao analisar frame")
        }
    }

    override fun close() = landmarker.close()
    companion object { const val MODEL_NAME = "hand_landmarker.task" }
}
