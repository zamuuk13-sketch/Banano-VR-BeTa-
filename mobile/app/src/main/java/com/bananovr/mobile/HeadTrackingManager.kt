package com.bananovr.mobile

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

class HeadTrackingManager(
    context: Context,
    private val listener: Listener
) : SensorEventListener {
    interface Listener { fun onHeadTrackingState(state: HeadTrackingState) }

    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val thread = HandlerThread("BananoVR-Tracking").apply { start() }
    private val handler = Handler(thread.looper)

    private val rotationSensor = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val gyroSensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val accelSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val linearAccelSensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val state = AtomicReference(HeadTrackingState.empty())
    private var running=false
    private var referenceQ=floatArrayOf(0f,0f,0f,1f)
    private var hasReference=false
    private var lastRotationTimestamp=0L
    private var lastAccelTimestamp=0L
    private var lastVelocityTimestamp=0L
    private var rateStart=0L
    private var rateSamples=0
    private var velocity=Vector3(0f,0f,0f)
    private var position=Vector3(0f,0f,0f)

    fun start() {
        if(running) return
        running=true
        handler.post {
            rotationSensor?.let { sm.registerListener(this,it,16667,handler) }
            gyroSensor?.let { sm.registerListener(this,it,16667,handler) }
            accelSensor?.let { sm.registerListener(this,it,16667,handler) }
            linearAccelSensor?.let { sm.registerListener(this,it,16667,handler) }
            state.set(state.get().copy(
                available=rotationSensor!=null,
                positionTrackingAvailable=linearAccelSensor!=null,
                sensorName=rotationSensor?.name ?: "Rotação não disponível"
            ))
        }
    }

    fun stop() {
        if(!running) return
        running=false
        handler.post { sm.unregisterListener(this) }
    }

    fun recenter() {
        val q=state.get().quaternion
        if(q.size>=4) {
            referenceQ=q.copyOf()
            hasReference=true
            position=Vector3(0f,0f,0f)
            velocity=Vector3(0f,0f,0f)
            state.set(state.get().copy(
                yawDegrees=0f,pitchDegrees=0f,rollDegrees=0f,
                position=position,velocity=velocity,recentered=true
            ))
        }
    }

    fun currentState()=state.get()

    override fun onSensorChanged(e: SensorEvent) {
        when(e.sensor.type) {
            Sensor.TYPE_GAME_ROTATION_VECTOR, Sensor.TYPE_ROTATION_VECTOR -> updateRotation(e)
            Sensor.TYPE_GYROSCOPE -> updateGyro(e)
            Sensor.TYPE_ACCELEROMETER -> updateAccelerometer(e)
            Sensor.TYPE_LINEAR_ACCELERATION -> updateLinearAcceleration(e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int)=Unit

    private fun updateRotation(e:SensorEvent) {
        val raw=FloatArray(4)
        SensorManager.getQuaternionFromVector(raw,e.values)
        val q=if(hasReference) multiply(inverse(referenceQ),raw) else raw
        val m=FloatArray(9)
        SensorManager.getRotationMatrixFromVector(m,q)
        val o=FloatArray(3)
        SensorManager.getOrientation(m,o)
        val yaw=Math.toDegrees(o[0].toDouble()).toFloat()
        val pitch=Math.toDegrees(o[1].toDouble()).toFloat()
        val roll=Math.toDegrees(o[2].toDouble()).toFloat()

        if(rateStart==0L) rateStart=e.timestamp
        rateSamples++
        val elapsed=e.timestamp-rateStart
        val hz=if(elapsed>=1_000_000_000L) {
            val v=rateSamples/(elapsed/1_000_000_000f)
            rateSamples=0; rateStart=e.timestamp; v
        } else state.get().updateRateHz

        lastRotationTimestamp=e.timestamp
        publish(state.get().copy(
            rotation=m,quaternion=q,yawDegrees=yaw,pitchDegrees=pitch,rollDegrees=roll,
            sensorTimestampNanos=e.timestamp,updateRateHz=hz,sensorName=e.sensor.name,available=true
        ))
    }

    private fun updateGyro(e:SensorEvent) {
        state.set(state.get().copy(
            gyro=Vector3(e.values[0],e.values[1],e.values[2]),sensorTimestampNanos=e.timestamp
        ))
    }

    private fun updateAccelerometer(e:SensorEvent) {
        state.set(state.get().copy(
            accelerometer=Vector3(e.values[0],e.values[1],e.values[2]),sensorTimestampNanos=e.timestamp
        ))
    }

    private fun updateLinearAcceleration(e:SensorEvent) {
        val dt=if(lastAccelTimestamp==0L) 0f
            else ((e.timestamp-lastAccelTimestamp)/1_000_000_000f).coerceIn(0f,0.05f)
        lastAccelTimestamp=e.timestamp
        if(dt>0f) {
            velocity=Vector3(
                velocity.x+e.values[0]*dt,
                velocity.y+e.values[1]*dt,
                velocity.z+e.values[2]*dt
            )
            // Small dead-zone to reduce integration noise while stationary.
            val dead=0.08f
            velocity=Vector3(
                if(abs(velocity.x)<dead) 0f else velocity.x,
                if(abs(velocity.y)<dead) 0f else velocity.y,
                if(abs(velocity.z)<dead) 0f else velocity.z
            )
            if(lastVelocityTimestamp!=0L) {
                position=Vector3(
                    position.x+velocity.x*dt,
                    position.y+velocity.y*dt,
                    position.z+velocity.z*dt
                )
            }
            lastVelocityTimestamp=e.timestamp
        }
        state.set(state.get().copy(
            linearAcceleration=Vector3(e.values[0],e.values[1],e.values[2]),
            position=position,velocity=velocity,sensorTimestampNanos=e.timestamp
        ))
    }

    private fun publish(s:HeadTrackingState) {
        state.set(s)
        listener.onHeadTrackingState(s)
    }

    private fun multiply(a:FloatArray,b:FloatArray):FloatArray {
        val ax=a[0];val ay=a[1];val az=a[2];val aw=a[3]
        val bx=b[0];val by=b[1];val bz=b[2];val bw=b[3]
        return floatArrayOf(
            aw*bx+ax*bw+ay*bz-az*by,
            aw*by-ax*bz+ay*bw+az*bx,
            aw*bz+ax*by-ay*bx+az*bw,
            aw*bw-ax*bx-ay*by-az*bz
        )
    }

    private fun inverse(q:FloatArray):FloatArray {
        val n=q[0]*q[0]+q[1]*q[1]+q[2]*q[2]+q[3]*q[3]
        if(n<=0.000001f)return floatArrayOf(0f,0f,0f,1f)
        return floatArrayOf(-q[0]/n,-q[1]/n,-q[2]/n,q[3]/n)
    }

    fun close(){stop();thread.quitSafely()}
}
