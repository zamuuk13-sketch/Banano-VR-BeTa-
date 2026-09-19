package com.bananovr.mobile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.math.max

class VRView(context: Context) : View(context) {
    private var state = HeadTrackingState.empty()
    private var ipd = 64f
    private var fov = 90f
    private var lensDistance = 42f
    private var vrScale = 1f
    private var left = true
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)

    fun update(s:HeadTrackingState){state=s;invalidate()}
    fun setConfig(ipdMm:Float,fovDeg:Float,lensMm:Float,scale:Float){ipd=ipdMm;fov=fovDeg;lensDistance=lensMm;vrScale=scale;invalidate()}

    override fun onDraw(c:Canvas){
        super.onDraw(c)
        c.drawColor(Color.BLACK)
        val w=width.toFloat(); val h=height.toFloat(); val half=w/2f
        val eye=if(left)0 else 1
        drawEye(c,0f,half,h,eye)
        drawEye(c,half,half,h,eye+1)
        p.color=Color.rgb(55,55,55);p.strokeWidth=2f;c.drawLine(half,0f,half,h,p)
        p.color=Color.WHITE;p.textSize=18f
        c.drawText("BANANOVR VR  •  IPD %.0f mm  •  FOV %.0f°".format(ipd,fov),16f,28f,p)
        p.textSize=14f;c.drawText("LENTE %.0f mm  •  ESCALA %.2fx".format(lensDistance,vrScale),16f,50f,p)
    }
    private fun drawEye(c:Canvas,x:Float,w:Float,h:Float,eye:Int){
        val cx=x+w/2f; val cy=h/2f
        val sep=(ipd/64f)*w*.055f
        p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.rgb(115,115,120)
        val size=minOf(w,h)*.26f*vrScale
        c.drawRect(cx-size-sep*(if(eye==0)1 else -1),cy-size,cx+size-sep*(if(eye==0)1 else -1),cy+size,p)
        p.style=Paint.Style.FILL;p.color=Color.WHITE;p.textSize=22f
        c.drawText(if(eye==0)"LEFT EYE" else "RIGHT EYE",cx-48f,cy,p)
        p.textSize=14f;c.drawText("Y %.1f°  P %.1f°".format(state.yawDegrees,state.pitchDegrees),cx-52f,cy+28f,p)
    }
}

