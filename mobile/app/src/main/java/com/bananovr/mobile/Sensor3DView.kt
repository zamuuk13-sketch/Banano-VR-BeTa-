package com.bananovr.mobile

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class Sensor3DView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs), Choreographer.FrameCallback {
    private var head = HeadTrackingState.empty()
    private var running = false
    private var renderFrames = 0
    private var lastFpsNanos = 0L
    private var renderFps = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 3f; textSize = 22f }

    fun start() { if (!running) { running = true; Choreographer.getInstance().postFrameCallback(this) } }
    fun stop() { running = false; Choreographer.getInstance().removeFrameCallback(this) }
    fun update(state: HeadTrackingState) { head = state }
    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        renderFrames++
        if (lastFpsNanos == 0L) lastFpsNanos = frameTimeNanos
        val dt = frameTimeNanos - lastFpsNanos
        if (dt >= 1_000_000_000L) { renderFps = renderFrames * 1_000_000_000f / dt; renderFrames=0; lastFpsNanos=frameTimeNanos }
        invalidate()
        Choreographer.getInstance().postFrameCallback(this)
    }
    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        c.drawColor(Color.rgb(12,12,14))
        val cx=width/2f; val cy=height/2f
        paint.style=Paint.Style.STROKE; paint.color=Color.rgb(70,70,75)
        val scale=minOf(width,height)*0.12f
        fun project(x:Float,y:Float,z:Float):PointF {
            val yaw=Math.toRadians(head.yawDegrees.toDouble()).toFloat()
            val pitch=Math.toRadians(head.pitchDegrees.toDouble()).toFloat()
            val cyaw=cos(yaw); val syaw=sin(yaw)
            val x1=x*cyaw-z*syaw; val z1=x*syaw+z*cyaw
            val cp=cos(pitch); val sp=sin(pitch)
            val y1=y*cp-z1*sp; val z2=y*sp+z1*cp
            val perspective=1f/(1f+z2*0.12f)
            return PointF(cx+x1*scale*perspective,cy-y1*scale*perspective)
        }
        val cube=arrayOf(
            floatArrayOf(-1f,-1f,-1f),floatArrayOf(1f,-1f,-1f),floatArrayOf(1f,1f,-1f),floatArrayOf(-1f,1f,-1f),
            floatArrayOf(-1f,-1f,1f),floatArrayOf(1f,-1f,1f),floatArrayOf(1f,1f,1f),floatArrayOf(-1f,1f,1f)
        ).map{project(it[0],it[1],it[2])}
        val edges=arrayOf(0 to 1,1 to 2,2 to 3,3 to 0,4 to 5,5 to 6,6 to 7,7 to 4,0 to 4,1 to 5,2 to 6,3 to 7)
        edges.forEach{e->c.drawLine(cube[e.first].x,cube[e.first].y,cube[e.second].x,cube[e.second].y,paint)}
        paint.style=Paint.Style.FILL;paint.color=Color.WHITE
        paint.textSize=26f;c.drawText("BANANOVR • 3D SENSOR TEST",24f,42f,paint)
        paint.textSize=19f
        c.drawText(String.format("HEAD  Y %.1f°  P %.1f°  R %.1f°",head.yawDegrees,head.pitchDegrees,head.rollDegrees),24f,72f,paint)
        c.drawText(String.format("SENSOR %.1f Hz  •  RENDER %.1f FPS",head.updateRateHz,renderFps),24f,98f,paint)
        c.drawText("Mova o celular para controlar a câmera 3D",24f,height-30f,paint)
    }
}