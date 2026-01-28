package com.hereliesaz.pwnagotchi.desktop

import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetAddress
import java.net.NetworkInterface

object DiscoveryManager {
    private var jmdns: JmDNS? = null
    private var serviceInfo: ServiceInfo? = null
    private const val PORT = 8765
    private const val TYPE = "_pwnagotchi._tcp.local."
    private const val NAME = "pwnagotchi-desktop"

    fun start() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Find a suitable IP address
                val addr = getLocalIpAddress() ?: InetAddress.getLocalHost()

                jmdns = JmDNS.create(addr)
                serviceInfo = ServiceInfo.create(TYPE, NAME, PORT, "Pwnagotchi Desktop")
                jmdns?.registerService(serviceInfo)
                println("MDNS Service registered: $NAME on $addr:$PORT")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        try {
            jmdns?.unregisterAllServices()
            jmdns?.close()
            jmdns = null
            println("MDNS Service stopped")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLocalIpAddress(): InetAddress? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue

                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is java.net.Inet4Address) {
                        return addr
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
