package com.hereliesaz.pwnagotchi.desktop

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit

object ProcessManager {
    private val userHome = System.getProperty("user.home")
    val workDir = File(userHome, ".pwnagotchi-desktop")
    private val requirementsFile = File(workDir, "requirements.txt")
    private val mainScript = File(workDir, "main.py")
    private val venvDir = File(workDir, "venv")

    private val isWindows = System.getProperty("os.name").lowercase().contains("win")

    private val pythonBin = if (isWindows)
        File(venvDir, "Scripts/python.exe")
    else
        File(venvDir, "bin/python3")

    private val pipBin = if (isWindows)
        File(venvDir, "Scripts/pip.exe")
    else
        File(venvDir, "bin/pip")

    private val _logs = MutableStateFlow("")
    val logs = _logs.asStateFlow()

    private val _status = MutableStateFlow("Stopped")
    val status = _status.asStateFlow()

    private var process: Process? = null

    fun isInstalled(): Boolean {
        return workDir.exists() && mainScript.exists() && pythonBin.exists()
    }

    fun install() {
        appendLog("Starting installation...")
        try {
            if (!workDir.exists()) workDir.mkdirs()

            val files = listOf("main.py", "requirements.txt", "README.md", "pwnagotchi.service", "test_main.py")

            files.forEach { fileName ->
                val stream = javaClass.getResourceAsStream("/pwnagotchi/$fileName")
                if (stream != null) {
                    val target = File(workDir, fileName)
                    Files.copy(stream, target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    appendLog("Extracted $fileName")
                } else {
                    appendLog("Failed to find resource: $fileName")
                }
            }

            if (!venvDir.exists()) {
                appendLog("Creating virtual environment...")
                // Try python3 first, then python
                if (!runCommand(listOf("python3", "-m", "venv", venvDir.absolutePath))) {
                    runCommand(listOf("python", "-m", "venv", venvDir.absolutePath))
                }
            }

            if (pipBin.exists()) {
                appendLog("Installing requirements...")
                runCommand(listOf(pipBin.absolutePath, "install", "-r", requirementsFile.absolutePath))
            } else {
                appendLog("Error: pip binary not found at ${pipBin.absolutePath}. Venv creation might have failed.")
            }

            appendLog("Installation complete.")
        } catch (e: Exception) {
            appendLog("Installation failed: ${e.message}")
            e.printStackTrace()
        }
    }

    fun start() {
        if (process != null && process!!.isAlive) {
            appendLog("Already running.")
            return
        }

        if (!pythonBin.exists()) {
            appendLog("Python binary not found. Please install first.")
            return
        }

        appendLog("Starting Pwnagotchi...")
        try {
            // Check for bettercap
            // We don't control bettercap, but we can warn
            // runCommand(listOf("which", "bettercap"))

            val pb = ProcessBuilder(pythonBin.absolutePath, mainScript.absolutePath)
            pb.directory(workDir)
            pb.redirectErrorStream(true)
            process = pb.start()
            _status.value = "Running"

            Thread {
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    appendLog("[PY] $line")
                }
                val exitCode = process?.waitFor() ?: -1
                _status.value = "Stopped (Exit code: $exitCode)"
                appendLog("Process exited with code $exitCode")
            }.start()

        } catch (e: Exception) {
            appendLog("Failed to start: ${e.message}")
            _status.value = "Error"
        }
    }

    fun stop() {
        process?.destroy()
        process = null
        _status.value = "Stopped"
        appendLog("Stopped.")
    }

    private fun runCommand(command: List<String>): Boolean {
        return try {
            val pb = ProcessBuilder(command)
            pb.redirectErrorStream(true)
            val p = pb.start()
            val reader = BufferedReader(InputStreamReader(p.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                appendLog("[CMD] $line")
            }
            p.waitFor(5, TimeUnit.MINUTES)
            p.exitValue() == 0
        } catch (e: Exception) {
            appendLog("Command failed: ${e.message}")
            false
        }
    }

    private fun appendLog(msg: String) {
        println(msg)
        // Keep logs limited size? Nah, simple for now.
        _logs.value = _logs.value + "\n" + msg
    }
}
