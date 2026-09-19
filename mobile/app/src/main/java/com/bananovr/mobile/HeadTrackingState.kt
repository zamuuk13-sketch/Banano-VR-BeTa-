package com.bananovr.mobile

data class Vector3(val x: Float, val y: Float, val z: Float)

data class HeadTrackingState(
    val rotation: FloatArray,
    val quaternion: FloatArray,
    val yawDegrees: Float,
    val pitchDegrees: Float,
    val rollDegrees: Float,
    val gyro: Vector3,
    val accelerometer: Vector3,
    val sensorTimestampNanos: Long,
    val updateRateHz: Float,
    val sensorName: String,
    val available: Boolean,
    val recentered: Boolean
) {
    companion object {
        fun empty() = HeadTrackingState(
            FloatArray(9), floatArrayOf(0f, 0f, 0f, 1f),
            0f, 0f, 0f, Vector3(0f,0f,0f), Vector3(0f,0f,0f),
            0L, 0f, "Indisponível", false, false
        )
    }
}
