# Pwnagotchi: Android Super App

This project's goal is to create the ultimate Android application for Pwnagotchi enthusiasts. It is a "Super App" that can operate in three distinct modes, providing a comprehensive mobile solution for both interacting with and *becoming* a Pwnagotchi.

## Project Goals

This application has three primary modes of operation:

### 1. Remote Client Mode
In this mode, the app acts as a feature-rich client and control panel for an external Pwnagotchi device (like a Raspberry Pi). This allows you to monitor and manage your Pwnagotchi from your phone. Features include:
-   **Live Status Dashboard:** A real-time view of your Pwnagotchi's mood, status, and activity.
-   **Handshake & Plugin Management:** View captured handshakes and manage the plugins on your device.
-   **Notifications:** Receive alerts for important events, like when a new handshake is captured.
-   **oPwngrid Integration:** Connect to the community and view leaderboards and stats.

### 2. Standalone Mode (Local Host)
In this mode, the app aims to turn a compatible, rooted Android device *into* a Pwnagotchi. This leverages the phone's own hardware to run the core Pwnagotchi functionalities natively.
-   **WiFi Monitor Mode:** Utilizes the device's WiFi chipset to scan for networks.
-   **Native `bettercap`:** Runs the `bettercap` toolset directly on Android to perform WiFi scanning and handshake capture.
-   **Full Pwnagotchi Experience:** Emulates the complete Pwnagotchi experience, including the character engine, UI, and background operation, all on your phone.

### 3. Hybrid Mode
This mode provides a powerful alternative for users whose Android devices do not have a Nexmon-compatible Wi-Fi chipset but who still desire a portable, phone-driven experience. In this hybrid architecture, the Android phone acts as the "brain" and user interface, while a connected Raspberry Pi serves as a dedicated, headless wireless peripheral responsible for all hardware-level network interaction.

## Requirements for Standalone Mode

To use the Standalone Mode, your Android device must meet the following requirements:

- **Root Access:** The application requires superuser permissions to run the necessary networking tools.
- **Busybox:** The `busybox` binary must be installed on your device. The recommended way to install it is through a Magisk module.
- **Bettercap:** The `bettercap` binary must be installed. Please refer to the in-app instructions for guidance on how to install it.

<p align="center">
  <small>Join the project community on our server!</small>
  <br/><br/>
  <a href="https://discord.gg/https://discord.gg/btZpkp45gQ" target="_blank" title="Join our community!">
    <img src="https://dcbadge.limes.pink/api/server/https://discord.gg/btZpkp45gQ"/>
  </a>
</p>
<hr/>
