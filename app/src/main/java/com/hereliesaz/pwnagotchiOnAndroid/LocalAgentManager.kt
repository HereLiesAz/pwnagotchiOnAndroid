package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LocalAgentManager(private val context: Context) {

    private val bettercapPath = "bettercap"
    private val busyboxPath = "busybox"

    companion object {
        private const val WLAN_INTERFACE_PREFIX = "wlan"
    }

    fun isDeviceRooted(): Boolean {
        return Shell.rootAccess()
    }

    fun hasNexmon(): Boolean {
        val result = Shell.su("which nexutil").exec()
        return result.isSuccess && result.out.isNotEmpty()
    }

    fun areBinariesInstalled(): Pair<Boolean, Boolean> {
        val bettercapResult = Shell.su("which bettercap").exec()
        val busyboxResult = Shell.su("which busybox").exec()
        return Pair(bettercapResult.isSuccess, busyboxResult.isSuccess)
    }

    fun getWirelessInterfaces(): List<String> {
        val result = Shell.su("$busyboxPath ifconfig -a").exec()
        if (result.isSuccess) {
            return result.out
                .mapNotNull { line ->
                    if (line.startsWith(WLAN_INTERFACE_PREFIX)) {
                        line.split(" ").firstOrNull()
                    } else {
                        null
                    }
                }
                .filter { it.isNotEmpty() }
                .distinct()
        }
        return emptyList()
    }

    fun enableMonitorMode(iface: String): Boolean {
        if (!Shell.su("svc wifi disable").exec().isSuccess) {
            return false
        }
        if (!Shell.su("$busyboxPath ifconfig $iface down").exec().isSuccess) {
            Shell.su("svc wifi enable").exec() // Revert
            return false
        }
        if (!Shell.su("nexutil -m2").exec().isSuccess) {
            Shell.su("$busyboxPath ifconfig $iface up").exec() // Revert
            Shell.su("svc wifi enable").exec() // Revert
            return false
        }
        if (!Shell.su("$busyboxPath ifconfig $iface up").exec().isSuccess) {
            Shell.su("nexutil -m0").exec() // Revert
            Shell.su("$busyboxPath ifconfig $iface down").exec() // Revert
            Shell.su("svc wifi enable").exec() // Revert
            return false
        }
        return true
    }

    fun disableMonitorMode(iface: String): Boolean {
        if (!Shell.su("nexutil -m0").exec().isSuccess) {
            return false
        }
        if (!Shell.su("$busyboxPath ifconfig $iface down").exec().isSuccess) {
            Shell.su("nexutil -m2").exec() // Revert
            return false
        }
        if (!Shell.su("$busyboxPath ifconfig $iface up").exec().isSuccess) {
            Shell.su("$busyboxPath ifconfig $iface down").exec() // Revert
            Shell.su("nexutil -m2").exec() // Revert
            return false
        }
        if (!Shell.su("svc wifi enable").exec().isSuccess) {
            // Attempt to revert, but this is the last step so it's less critical
            Shell.su("svc wifi disable").exec()
            return false
        }
        return true
    }

    fun startBettercap(iface: String): Shell.Result {
        val logFile = File(context.cacheDir, "bettercap.log").absolutePath
        val command = "$bettercapPath -iface $iface -debug -api-addr 127.0.0.1:8080 > $logFile 2>&1 &"
        return Shell.su(command).exec()
    }

    fun stopBettercap(): Shell.Result {
        return Shell.su("$busyboxPath pkill bettercap").exec()
    }

    fun configureUsbNetwork(): Shell.Result {
        return Shell.su(
            "svc usb setFunctions rndis",
            "ip link set dev rndis0 up",
            "ip addr add 10.0.0.1/24 dev rndis0"
        ).exec()
    }
}
