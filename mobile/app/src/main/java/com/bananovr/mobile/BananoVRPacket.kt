package com.bananovr.mobile

import org.json.JSONArray
import org.json.JSONObject

data class BananoVRPacket(
    val timestampNanos: Long,
    val head: HeadTrackingState,
    val hands: HandTrackingState?,
    val batteryPercent: Int = -1
) {
    fun toJson(): String {
        val h=JSONObject()
        h.put("timestampNanos",timestampNanos)
        h.put("yaw",head.yawDegrees); h.put("pitch",head.pitchDegrees); h.put("roll",head.rollDegrees)
        h.put("position",JSONArray(listOf(head.position.x,head.position.y,head.position.z)))
        h.put("velocity",JSONArray(listOf(head.velocity.x,head.velocity.y,head.velocity.z)))
        h.put("quaternion",JSONArray(head.quaternion.toList()))
        h.put("sensorHz",head.updateRateHz)
        val root=JSONObject()
        root.put("type","bananovr.tracking")
        root.put("version",1)
        root.put("timestampNanos",timestampNanos)
        root.put("head",h)
        root.put("hands",JSONArray().apply {
            hands?.hands?.forEach { hand ->
                put(JSONObject().apply {
                    put("side",hand.side.name); put("score",hand.score); put("gesture",hand.gesture.name)
                    put("timestampNanos",hand.timestampNanos)
                    put("landmarks",JSONArray().apply { hand.landmarks.forEach { p->put(JSONArray(listOf(p.x,p.y,p.z))) } })
                })
            }
        })
        root.put("batteryPercent",batteryPercent)
        return root.toString()
    }
}
