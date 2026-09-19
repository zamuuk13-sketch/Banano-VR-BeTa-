package com.bananovr.mobile

data class HandPoint3D(val x: Float, val y: Float, val z: Float)
enum class HandSide { LEFT, RIGHT, UNKNOWN }
enum class HandGesture { OPEN, FIST, POINT, PINCH, THUMBS_UP, UNKNOWN }

data class TrackedHand(
    val side: HandSide,
    val score: Float,
    val landmarks: List<HandPoint3D>,
    val gesture: HandGesture,
    val timestampNanos: Long
)

data class HandTrackingState(
    val hands: List<TrackedHand>,
    val timestampNanos: Long,
    val frameTimeMs: Long
)
