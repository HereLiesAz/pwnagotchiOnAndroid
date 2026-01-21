package com.hereliesaz.pwnagotchiOnAndroid

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LocalAgentManager(private val context: Context) {

    private val bettercapPath: String
        get() = File(context.filesDir, "bettercap").absolutePath

    private val busyboxPath: String
        get() = File(context.filesDir, "busybox").absolutePath

    companion object {
        private const val WLAN_INTERFACE_PREFIX = "wlan"
    }

    fun isDeviceRooted(): Boolean {
        return Shell.rootAccess()
    }

    fun requestRootAccess(callback: (Boolean) -> Unit) {
        Shell.getShell { shell ->
            callback(shell.isRoot)
        }
    }

    fun hasNexmon(): Boolean {
        val result = Shell.cmd("which nexutil").exec()
        return result.isSuccess && result.out.isNotEmpty()
    }

    fun installNexmon(): Boolean {
        val commands = listOf(
            Command("apt-get update", "echo 'apt-get update failed'"),
            Command("apt-get install -y nexmon", "echo 'nexmon installation failed'")
        )
        return executeCommands(commands)
    }

    fun extractAssets(): Boolean {
        return try {
            val assets = listOf("bettercap", "busybox")
            assets.forEach { fileName ->
                val file = File(context.filesDir, fileName)
                // Always overwrite to ensure we have the latest version from the APK
                context.assets.open(fileName).use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                Shell.cmd("chmod 755 ${file.absolutePath}").exec()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun areBinariesInstalled(): Pair<Boolean, Boolean> {
        val bettercapExists = File(bettercapPath).exists()
        val busyboxExists = File(busyboxPath).exists()
        return Pair(bettercapExists, busyboxExists)
    }

    fun getWirelessInterfaces(): List<String> {
        val result = Shell.cmd("$busyboxPath ifconfig -a").exec()
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

    private data class Command(val execute: String, val rollback: String)

    private fun executeCommands(commands: List<Command>): Boolean {
        val successfulCommands = mutableListOf<Command>()
        for (command in commands) {
            if (Shell.cmd(command.execute).exec().isSuccess) {
                successfulCommands.add(command)
            } else {
                // Rollback in reverse order
                successfulCommands.reversed().forEach { Shell.cmd(it.rollback).exec() }
                return false
            }
        }
        return true
    }

    fun enableMonitorMode(iface: String): Boolean {
        val commands = listOf(
            Command("svc wifi disable", "svc wifi enable"),
            Command("$busyboxPath ifconfig $iface down", "$busyboxPath ifconfig $iface up"),
            Command("nexutil -m2", "nexutil -m0"),
            Command("$busyboxPath ifconfig $iface up", "$busyboxPath ifconfig $iface down")
        )
        return executeCommands(commands)
    }

    fun disableMonitorMode(iface: String): Boolean {
        val commands = listOf(
            Command("nexutil -m0", "nexutil -m2"),
            Command("$busyboxPath ifconfig $iface down", "$busyboxPath ifconfig $iface up"),
            Command("$busyboxPath ifconfig $iface up", "$busyboxPath ifconfig $iface down"),
            Command("svc wifi enable", "svc wifi disable")
        )
        return executeCommands(commands)
    }

    fun startBettercap(iface: String): Shell.Result {
        val logFile = File(context.cacheDir, "bettercap.log").absolutePath
        val command = "$bettercapPath -iface $iface -debug -api-addr 127.0.0.1:8080 > $logFile 2>&1 &"
        return Shell.cmd(command).exec()
    }

    fun stopBettercap(): Shell.Result {
        return Shell.cmd("$busyboxPath pkill bettercap").exec()
    }

    fun startPythonAi() {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        Thread {
            try {
                val python = Python.getInstance()
                val aiModule = python.getModule("ai")
                if (aiModule.containsKey("run")) {
                    aiModule.callAttr("run")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun configureUsbNetwork(): Shell.Result {
        return Shell.cmd(
            "svc usb setFunctions rndis",
            "ip link set dev rndis0 up",
            "ip addr add 10.0.0.2/24 dev rndis0"
        ).exec()
    }
}
