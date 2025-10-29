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
        val result = Shell.cmd("which nexutil").exec()
        return result.isSuccess && result.out.isNotEmpty()
    }

    fun areBinariesInstalled(): Pair<Boolean, Boolean> {
        val bettercapResult = Shell.cmd("which bettercap").exec()
        val busyboxResult = Shell.cmd("which busybox").exec()
        return Pair(bettercapResult.isSuccess, busyboxResult.isSuccess)
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

    fun configureUsbNetwork(): Shell.Result {
        return Shell.cmd(
            "svc usb setFunctions rndis",
            "ip link set dev rndis0 up",
            "ip addr add 10.0.0.1/24 dev rndis0"
        ).exec()
    }
}
