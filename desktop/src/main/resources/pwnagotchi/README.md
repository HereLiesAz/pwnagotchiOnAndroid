# Pwnagotchi Raspberry Pi WiFi Adapter

This application turns a Raspberry Pi into a dedicated, isolated WiFi adapter with monitor mode, designed to work with the Pwnagotchi Android application's Hybrid Mode.

## Prerequisites

- A Raspberry Pi with a compatible WiFi adapter.
- Raspberry Pi OS (32-bit or 64-bit).
- `bettercap` installed and available at `/usr/local/bin/bettercap`.
- Python 3.

## Installation

1.  **Clone the repository:**
    ```bash
    git clone <repository_url>
    cd pwnagotchi_raspi
    ```

2.  **Install Python dependencies:**
    ```bash
    pip install -r requirements.txt
    ```

3.  **Generate SSL Certificate:**
    You need to generate a self-signed SSL certificate and key for the secure WebSocket server.
    ```bash
    openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes -subj "/C=US/ST=California/L=San Francisco/O=Pwnagotchi/OU=Pwnagotchi/CN=pwnagotchi.local"
    ```

4.  **Move the application to `/opt`:**
    ```bash
    sudo mv . /opt/pwnagotchi_raspi
    ```

5.  **Install and enable the `systemd` service:**
    ```bash
    sudo cp /opt/pwnagotchi_raspi/pwnagotchi.service /etc/systemd/system/
    sudo systemctl enable pwnagotchi.service
    sudo systemctl start pwnagotchi.service
    ```

## Usage

Once the service is running, the Raspberry Pi will automatically enable monitor mode and start `bettercap`. You can then connect your Android device to the Raspberry Pi via USB and use the Pwnagotchi Android app in Hybrid Mode. The app will connect to the WebSocket server running on the Raspberry Pi and start receiving data.
