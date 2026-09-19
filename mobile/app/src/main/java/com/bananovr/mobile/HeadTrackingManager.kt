package com.bananovr.mobile

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.atomic.AtomicReference

class HeadTrackingManager(
    context: Context,
    private val listener: Listener
) : SensorEventListener {

    interface Listener { fun onHeadTrackingState(state: HeadTrackingState) }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorThread = HandlerThread("BananoVR-Sensors").apply { start() }
    private val sensorHandler = Handler(sensorThread.looper)

    private val orientationSensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val state = AtomicReference(HeadTrackingState.empty())
    private var running = false
    private var referenceQuaternion = floatArrayOf(0f, 0f, 0f, 1f)
    private var hasReference = false
    private var rateStartNanos = 0L
    private var rateSamples = 0

    fun start() {
        if (running) return
        running = true
        sensorHandler.post {
            orientationSensor?.let { sensorManager.registerListener(this, it, 16_667, sensorHandler) }
            gyroSensor?.let { sensorManager.registerListener(this, it, 16_667, sensorHandler) }
            accelerometerSensor?.let { sensorManager.registerListener(this, it, 16_667, sensorHandler) }
            state.set(state.get().copy(
                available = orientationSensor != null,
                sensorName = orientationSensor?.name ?: "Rotação não disponível"
            ))
        }
    }

    fun stop() {
        if (!running) return
        running = false
        sensorHandler.post { sensorManager.unregisterListener(this) }
    }

    fun recenter() {
        val q = state.get().quaternion
        if (q.size >= 4) {
            referenceQuaternion = q.copyOf()
            hasReference = true
            state.set(state.get().copy(yawDegrees = 0f, pitchDegrees = 0f, rollDegrees = 0f, recentered = true))
        }
    }

    fun currentState(): HeadTrackingState = state.get()

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_GAME_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR -> updateRotation(event)
            Sensor.TYPE_GYROSCOPE -> updateGyro(event)
            Sensor.TYPE_ACCELEROMETER -> updateAccelerometer(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun updateRotation(event: SensorEvent) {
        val raw = FloatArray(4)
        SensorManager.getQuaternionFromVector(raw, event.values)
        val relative = if (hasReference) quaternionMultiply(quaternionInverse(referenceQuaternion), raw) else raw
        val matrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(matrix, relative)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(matrix, orientation)

        val yaw = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

        if (rateStartNanos == 0L) rateStartNanos = event.timestamp
        rateSamples++
        val elapsed = event.timestamp - rateStartNanos
        val hz = if (elapsed >= 1_000_000_000L) {
            val value = rateSamples / (elapsed / 1_000_000_000f)
            rateSamples = 0
            rateStartNanos = event.timestamp
            value
        } else state.get().updateRateHz

        val next = state.get().copy(
            rotation = matrix, quaternion = relative,
            yawDegrees = yaw, pitchDegrees = pitch, rollDegrees = roll,
            sensorTimestampNanos = event.timestamp, updateRateHz = hz,
            sensorName = event.sensor.name, available = true
        )
        state.set(next)
        if (elapsed >= 1_000_000_000L) listener.onHeadTrackingState(next)
    }

    private fun updateGyro(event: SensorEvent) {
        state.set(state.get().copy(
            gyro = Vector3(event.values[0], event.values[1], event.values[2]),
            sensorTimestampNanos = event.timestamp
        ))
    }

    private fun updateAccelerometer(event: SensorEvent) {
        state.set(state.get().copy(
            accelerometer = Vector3(event.values[0], event.values[1], event.values[2]),
            sensorTimestampNanos = event.timestamp
        ))
    }

    private fun quaternionMultiply(a: FloatArray, b: FloatArray): FloatArray {
        val ax=a[0]; val ay=a[1]; val az=a[2]; val aw=a[3]
        val bx=b[0]; val by=b[1]; val bz=b[2]; val bw=b[3]
        return floatArrayOf(
            aw*bx + ax*bw + ay*bz - az*by,
            aw*by - ax*bz + ay*bw + az*bx,
            aw*bz + ax*by - ay*bx + az*bw,
            aw*bw - ax*bx - ay*by - az*bz
        )
    }

    private fun quaternionInverse(q: FloatArray): FloatArray {
        val n = q[0]*q[0] + q[1]*q[1] + q[2]*q[2] + q[3]*q[3]
        if (n <= 0.000001f) return floatArrayOf(0f,0f,0f,1f)
        return floatArrayOf(-q[0]/n, -q[1]/n, -q[2]/n, q[3]/n)
    }

    fun close() {
        stop()
        sensorThread.quitSafely()
    }
}
