# Pwnagotchi Desktop Application

This is a modern, cross-platform desktop application for Pwnagotchi, built with Kotlin Multiplatform (Compose Desktop). It brings the Pwnagotchi experience to your Linux desktop (ARM64, ARM, AMD64, i386) and other supported operating systems.

## Features

*   **Standalone Operation:** Runs as a standalone desktop application. No need for a headless Raspberry Pi.
*   **Modern UI:** A clean, responsive Material Design 3 interface built with Jetpack Compose.
*   **Pwnagotchi Integration:**
    *   Bundles and manages a Python environment for the Pwnagotchi backend.
    *   Integrates with `bettercap` for WiFi monitoring (requires `bettercap` installed on the system).
    *   Falls back to a "Blind" simulation mode if `bettercap` is unavailable or permissions are missing.
*   **Remote Pairing:** Easily pair with the Pwnagotchi Android app.
    *   **Auto-Discovery:** Broadcasts its presence on the local network using mDNS (`_pwnagotchi._tcp.local.`).
    *   **QR Code Pairing:** Generates a QR code for quick connection setup.
*   **Plugin Management:** View and toggle plugins directly from the desktop UI.

## Requirements

*   **Java Runtime Environment (JRE):** Java 11 or higher (Java 17 recommended).
*   **Python 3:** The application requires Python 3 to be installed on your system.
*   **Bettercap (Optional but Recommended):** For full functionality (WiFi monitoring, packet capture), `bettercap` must be installed and accessible.
    *   **Kali Linux:** `sudo apt install bettercap`
    *   **Permissions:** The application backend requires `sudo` privileges to run `bettercap` and control network interfaces. Ensure your user has `sudo` access or configure passwordless sudo for `bettercap` if you wish to run it seamlessly.

## Installation & Running

### From Source

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/HereLiesAz/pwnagotchiOnAndroid.git
    cd pwnagotchiOnAndroid
    ```

2.  **Run the application:**
    ```bash
    ./gradlew :desktop:run
    ```

### Building Distribution (Deb/Rpm)

To create an installable package for your distribution:

```bash
# For Debian/Ubuntu (.deb)
./gradlew :desktop:packageDeb

# For RedHat/Fedora (.rpm)
./gradlew :desktop:packageRpm
```

The output packages will be located in `desktop/build/compose/binaries/main/`.

## Usage

1.  **Launch the App:** Open "Pwnagotchi Desktop" from your application menu or command line.
2.  **Initial Setup:** On the first run, the app will set up a local Python virtual environment in `~/.pwnagotchi-desktop`. This may take a few moments.
3.  **Start Pwnagotchi:** The service should start automatically. If not, go to the **Settings** tab and click "Start".
    *   If `bettercap` is installed and permissions are correct, you will see the Pwnagotchi face and stats update.
    *   If not, you will see a "Blind" mode face, indicating the UI is working but WiFi monitoring is disabled.
4.  **Pairing with Android:**
    *   Go to the **Settings** tab in the Desktop app.
    *   Scan the QR code with your Pwnagotchi Android app, or wait for the Android app to auto-discover "Pwnagotchi Desktop" on your WiFi network.
