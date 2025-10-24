package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream

class LocalAgentManager(private val context: Context) {
    fun isDeviceRooted(): Boolean {
        return Shell.rootAccess()
    }

    fun hasNexmon(): Boolean {
        val result = Shell.su("which nexutil").exec()
        return result.isSuccess && result.out.isNotEmpty()
    }

    fun installBettercap() {
        val bettercapFile = File(context.filesDir, "bettercap")
        if (!bettercapFile.exists()) {
            context.assets.open("bettercap").use { inputStream ->
                FileOutputStream(bettercapFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            Shell.cmd("chmod +x ${bettercapFile.absolutePath}").exec()
        }
    }

    fun enableMonitorMode(): Boolean {
        Shell.su("svc wifi disable").exec()
        val result = Shell.su("nexutil -m2").exec()
        return result.isSuccess
    }

    fun disableMonitorMode(): Boolean {
        Shell.su("nexutil -m0").exec()
        val result = Shell.su("svc wifi enable").exec()
        return result.isSuccess
    }

    fun startBettercap(): Shell.Result {
        val bettercapFile = File(context.filesDir, "bettercap")
        return Shell.su("${bettercapFile.absolutePath} -iface wlan0").exec()
    }

    fun stopBettercap() {
        Shell.su("killall bettercap").exec()
    }

    fun configureUsbNetwork(): Shell.Result {
        return Shell.su(
            "svc usb setFunctions rndis",
            "ip link set dev rndis0 up",
            "ip addr add 10.0.0.1/24 dev rndis0"
        ).exec()
    }
}
