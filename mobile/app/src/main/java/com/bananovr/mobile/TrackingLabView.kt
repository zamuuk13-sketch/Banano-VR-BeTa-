package com.bananovr.mobile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class TrackingLabView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var hands: HandTrackingState? = null
    private var head = HeadTrackingState.empty()
    private val panel=Paint(Paint.ANTI_ALIAS_FLAG)
    private val border=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeWidth=2f}
    private val title=Paint(Paint.ANTI_ALIAS_FLAG).apply{textSize=30f;isFakeBoldText=true}
    private val text=Paint(Paint.ANTI_ALIAS_FLAG).apply{textSize=24f}
    private val small=Paint(Paint.ANTI_ALIAS_FLAG).apply{textSize=19f}
    private val dot=Paint(Paint.ANTI_ALIAS_FLAG)
    fun update(h:HandTrackingState?,s:HeadTrackingState){hands=h;head=s;postInvalidateOnAnimation()}
    override fun onDraw(c:Canvas){
        super.onDraw(c);val w=width.toFloat();val h=height.toFloat()
        panel.color=0xCC101010.toInt();c.drawRect(0f,0f,w,h,panel)
        title.color=0xFFFFFFFF.toInt();c.drawText("BANANOVR • TRACKING LAB",28f,48f,title)
        card(c,24f,70f,w-24f,245f,"HEAD TRACKING")
        line(c,"Status",if(head.available)"ONLINE" else "OFFLINE",45f,108f)
        line(c,"Sensor",head.sensorName.take(24),45f,136f)
        line(c,"Rate",String.format("%.1f Hz",head.updateRateHz),45f,164f)
        line(c,"Yaw / Pitch / Roll",String.format("%.1f° / %.1f° / %.1f°",head.yawDegrees,head.pitchDegrees,head.rollDegrees),45f,192f)
        line(c,"Position",String.format("%.3f / %.3f / %.3f m",head.position.x,head.position.y,head.position.z),45f,220f)
        val top=265f;val cardH=max(180f,(h-top-24f)/2f);card(c,24f,top,w-24f,top+cardH,"HANDS")
        if(hands==null||hands!!.hands.isEmpty()){small.color=0xFFBBBBBB.toInt();c.drawText("Nenhuma mão detectada",45f,top+62f,small)}
        else hands!!.hands.take(2).forEachIndexed{index,hand->
            val y=top+60f+index*62f;dot.color=if(hand.side==HandSide.LEFT)0xFF66CCFF.toInt() else 0xFFFFCC66.toInt()
            c.drawCircle(52f,y-7f,9f,dot);small.color=0xFFFFFFFF.toInt()
            c.drawText(hand.side.toString()+"  "+(hand.score*100).toInt()+"%  "+hand.gesture.toString(),72f,y,small)
            c.drawText("21 joints • XYZ",72f,y+25f,small)
        }
        val bottom=top+cardH+20f;card(c,24f,bottom,w-24f,h-24f,"IMU / DEVICE")
        line(c,"Gyro",String.format("%.2f  %.2f  %.2f",head.gyro.x,head.gyro.y,head.gyro.z),45f,bottom+48f)
        line(c,"Accel",String.format("%.2f  %.2f  %.2f",head.accelerometer.x,head.accelerometer.y,head.accelerometer.z),45f,bottom+76f)
        line(c,"Position tracking",if(head.positionTrackingAvailable)"AVAILABLE" else "ESTIMATED / LIMITED",45f,bottom+104f)
        line(c,"Recenter",if(head.recentered)"ACTIVE" else "NOT CALIBRATED",45f,bottom+132f)
    }
    private fun card(c:Canvas,l:Float,t:Float,r:Float,b:Float,label:String){panel.color=0xE61A1A1A.toInt();c.drawRoundRect(RectF(l,t,r,b),18f,18f,panel);border.color=0xFF444444.toInt();c.drawRoundRect(RectF(l,t,r,b),18f,18f,border);small.color=0xFFAAAAAA.toInt();c.drawText(label,l+20f,t+28f,small)}
    private fun line(c:Canvas,label:String,value:String,x:Float,y:Float){small.color=0xFFAAAAAA.toInt();text.color=0xFFFFFFFF.toInt();c.drawText(label,x,y,text);c.drawText(value,min(width-30f,x+150f),y,small)}
}