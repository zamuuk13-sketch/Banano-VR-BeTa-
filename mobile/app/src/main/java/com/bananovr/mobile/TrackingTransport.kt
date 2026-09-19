package com.bananovr.mobile

import android.content.Context
import android.net.wifi.WifiManager
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean

class TrackingTransport(private val context: Context) {
    companion object { const val DEFAULT_PORT=27182; const val DISCOVERY_PORT=27183 }
    private var socket: DatagramSocket?=null
    private var host: InetAddress?=null
    private val running=AtomicBoolean(false)

    fun connect(ip:String,port:Int=DEFAULT_PORT):Boolean=try {
        close(); host=InetAddress.getByName(ip); socket=DatagramSocket(); socket?.broadcast=true; running.set(true); true
    } catch(_:Exception){false}

    fun autoDiscover(timeoutMs:Int=700):String? {
        return try {
            val wifi=context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val dhcp=wifi.dhcpInfo
            val mask=dhcp.netmask
            val broadcast=(dhcp.ipAddress and mask) or mask.inv()
            val address=InetAddress.getByAddress(byteArrayOf((broadcast and 255).toByte(),((broadcast shr 8) and 255).toByte(),((broadcast shr 16) and 255).toByte(),((broadcast shr 24) and 255).toByte()))
            val s=DatagramSocket(); s.broadcast=true; s.soTimeout=timeoutMs
            val msg="{"type":"bananovr.discover","version":1}".toByteArray()
            s.send(DatagramPacket(msg,msg.size,address,DISCOVERY_PORT))
            val buffer=ByteArray(2048)
            val packet=DatagramPacket(buffer,buffer.size)
            try { s.receive(packet); packet.address.hostAddress } catch(_:SocketTimeoutException){ null } finally { s.close() }
        } catch(_:Exception){null}
    }

    fun send(packet:BananoVRPacket):Boolean=try {
        val data=packet.toJson().toByteArray(Charsets.UTF_8); val target=host ?: return false
        socket?.send(DatagramPacket(data,data.size,target,DEFAULT_PORT)); true
    }
    fun close(){running.set(false);socket?.close();socket=null;host=null}
}
