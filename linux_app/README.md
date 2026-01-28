# Pwnagotchi Linux Desktop App

A fully functional Pwnagotchi client for Linux desktops (ARM64, ARM, AMD64, i386).

## Features
- **Modern GUI**: PyQt6-based interface with authentic Pwnagotchi faces and stats.
- **Cross-Platform**: Runs on any Linux distro supporting Python 3 and Qt6.
- **Remote Management**: Hosts a Secure WebSocket server to connect with the Android App (in Remote Mode).
- **Core Logic**: Implements the Pwnagotchi state machine (AI/Stats).

## Prerequisites
- Python 3.9+
- `bettercap` installed and running (default: `ws://127.0.0.1:8080/api/events`).
- `PyQt6` (recommended via system package manager, e.g., `apt install python3-pyqt6`, or via pip).

## Installation

1.  **Install Dependencies:**
    From the root of the repository:
    ```bash
    pip install -r linux_app/requirements.txt
    ```
    *Note: It is recommended to create a virtual environment.*

2.  **Setup Binaries (Bettercap):**
    Run the setup script to download and install the Bettercap binary for your architecture:
    ```bash
    bash linux_app/setup_dependencies.sh
    ```

3.  **Generate SSL Certificates:**
    The Android app requires a Secure WebSocket connection. Generate self-signed certificates in the `linux_app/` directory:
    ```bash
    cd linux_app
    openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes -subj "/C=US/ST=California/L=San Francisco/O=Pwnagotchi/OU=Pwnagotchi/CN=pwnagotchi.local"
    cd ..
    ```

## Usage

1.  **Start Bettercap** (if using real hardware):
    Ensure Bettercap is running and its API is accessible at `127.0.0.1:8080`.

2.  **Run the App:**
    From the root of the repository:
    ```bash
    python3 -m linux_app.main
    ```

## Connecting Android App

1.  Open the Pwnagotchi Android App.
2.  Go to Settings -> Connection Mode -> **Remote**.
3.  Enter the IP address of your Linux machine.
4.  The app should connect and mirror the stats/face.
