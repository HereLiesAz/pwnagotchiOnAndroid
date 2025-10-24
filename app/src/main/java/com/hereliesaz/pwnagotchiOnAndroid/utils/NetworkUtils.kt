package com.hereliesaz.pwnagotchiOnAndroid.utils

import java.net.NetworkInterface
import java.util.Collections

object NetworkUtils {
    fun getWifiInterfaceName(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.name.startsWith("wlan")) {
                    return intf.name
                }
            }
        } catch (ex: Exception) {
            // Ignore
        }
        return "wlan0"
    }
}
