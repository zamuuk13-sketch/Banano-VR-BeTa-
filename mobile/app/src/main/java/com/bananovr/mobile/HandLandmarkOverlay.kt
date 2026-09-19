package com.bananovr.mobile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

class HandLandmarkOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private var result: HandLandmarkerResult? = null

    fun setResult(newResult: HandLandmarkerResult?) {
        result = newResult
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val current = result ?: return
        val sx = width.toFloat()
        val sy = height.toFloat()

        current.landmarks().forEach { hand ->
            val points = hand.map {
                android.graphics.PointF(it.x() * sx, it.y() * sy)
            }

            HAND_CONNECTIONS.forEach { (a, b) ->
                if (a < points.size && b < points.size) {
                    canvas.drawLine(points[a].x, points[a].y, points[b].x, points[b].y, linePaint)
                }
            }

            points.forEach { canvas.drawCircle(it.x, it.y, 7f, pointPaint) }
        }
    }

    companion object {
        private val HAND_CONNECTIONS = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 4,
            0 to 5, 5 to 6, 6 to 7, 7 to 8,
            0 to 9, 9 to 10, 10 to 11, 11 to 12,
            0 to 13, 13 to 14, 14 to 15, 15 to 16,
            0 to 17, 17 to 18, 18 to 19, 19 to 20,
            5 to 9, 9 to 13, 13 to 17
        )
    }
}
