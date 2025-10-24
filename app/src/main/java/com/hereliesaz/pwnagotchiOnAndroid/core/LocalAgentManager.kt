package com.hereliesaz.pwnagotchiOnAndroid.core

import com.topjohnwu.superuser.Shell

/**
 * A manager class responsible for handling all root command execution for the local agent.
 *
 * This class will encapsulate the logic for enabling/disabling monitor mode, running
 * `bettercap`, and other root-required operations.
 */
object LocalAgentManager {
    private const val WLAN_INTERFACE = "wlan0"

    suspend fun enableMonitorMode(): Boolean {
        // Method 1: Qualcomm con_mode (More reliable on some devices)
        Shell.cmd("echo 4 > /sys/module/wlan/parameters/con_mode").exec()
        if (verifyMonitorMode()) return true

        // Method 2: Standard iwconfig fallback
        Shell.cmd(
            "ip link set $WLAN_INTERFACE down",
            "iwconfig $WLAN_INTERFACE mode monitor",
            "ip link set $WLAN_INTERFACE up"
        ).exec()
        return verifyMonitorMode()
    }

    suspend fun disableMonitorMode(): Boolean {
        Shell.cmd(
            "ip link set $WLAN_INTERFACE down",
            "iwconfig $WLAN_INTERFACE mode managed",
            "ip link set $WLAN_INTERFACE up"
        ).exec()
        return !verifyMonitorMode()
    }

    private suspend fun verifyMonitorMode(): Boolean {
        val result = Shell.cmd("iwconfig $WLAN_INTERFACE").exec()
        return result.out.any { it.contains("Mode:Monitor", ignoreCase = true) }
    }
}
