package com.bananovr.mobile

import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.sqrt

class HandTrackingProcessor {
    fun process(result: HandLandmarkerResult, timestampNanos: Long): HandTrackingState {
        val hands = result.landmarks().mapIndexed { index, landmarks ->
            val category = result.handednesses().getOrNull(index)?.firstOrNull()
            val side = when (category?.categoryName()?.lowercase()) {
                "left" -> HandSide.LEFT
                "right" -> HandSide.RIGHT
                else -> HandSide.UNKNOWN
            }
            val score = category?.score() ?: 0f
            val points = landmarks.map { HandPoint3D(it.x(), it.y(), it.z()) }
            TrackedHand(side, score, points, classifyGesture(points), timestampNanos)
        }
        return HandTrackingState(hands, timestampNanos, 0L)
    }

    private fun classifyGesture(p: List<HandPoint3D>): HandGesture {
        if (p.size < 21) return HandGesture.UNKNOWN
        fun d(a: Int, b: Int): Float {
            val x = p[a].x - p[b].x
            val y = p[a].y - p[b].y
            val z = p[a].z - p[b].z
            return sqrt(x*x + y*y + z*z)
        }
        val palm = d(0, 9).coerceAtLeast(0.0001f)
        if (d(4, 8) < palm * 0.55f) return HandGesture.PINCH
        val extended = listOf(
            d(8, 5) > palm * 0.70f,
            d(12, 9) > palm * 0.70f,
            d(16, 13) > palm * 0.70f,
            d(20, 17) > palm * 0.70f
        )
        return when {
            extended.all { it } -> HandGesture.OPEN
            extended.none { it } -> HandGesture.FIST
            extended[0] && !extended[1] && !extended[2] && !extended[3] -> HandGesture.POINT
            else -> HandGesture.UNKNOWN
        }
    }
}
