package com.bananovr.mobile

import android.content.Context
import android.net.wifi.WifiManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean

class TrackingTransport(private val context: Context) {
    companion object { const val DEFAULT_PORT=27182 }
    private var socket: DatagramSocket?=null
    private var host: InetAddress?=null
    private val running=AtomicBoolean(false)

    fun connect(ip:String,port:Int=DEFAULT_PORT):Boolean=try {
        host=InetAddress.getByName(ip); socket=DatagramSocket(); socket?.broadcast=true; running.set(true); true
    } catch(_:Exception){false}

    fun send(packet:BananoVRPacket):Boolean=try {
        val data=packet.toJson().toByteArray(Charsets.UTF_8)
        val target=host ?: return false
        socket?.send(DatagramPacket(data,data.size,target,DEFAULT_PORT)); true
    } catch(_:Exception){false}

    fun close(){running.set(false);socket?.close();socket=null;host=null}
}
