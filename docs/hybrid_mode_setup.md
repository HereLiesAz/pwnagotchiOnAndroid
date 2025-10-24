# Hybrid Mode: Raspberry Pi Setup Guide

This guide details how to configure a Raspberry Pi to act as a dedicated wireless interface for the Pwnagotchi Android app's Hybrid Mode.

## Prerequisites

- A Raspberry Pi (Zero W, 3A+, 3B+, 4B, etc.)
- A microSD card (8GB or larger)
- A USB cable to connect the Raspberry Pi to your Android device
- Raspberry Pi OS (32-bit or 64-bit) installed and configured with SSH access.
- `git` installed (`sudo apt-get install git`)

## 1. Install `bettercap`

Follow the official `bettercap` documentation to compile and install the latest version from source. Ensure the compiled binary is located at `/usr/local/bin/bettercap`.

[https://www.bettercap.org/installation/#compiling-from-source](https://www.bettercap.org/installation/#compiling-from-source)

## 2. Install the Pwnagotchi Raspberry Pi Service

The `pwnagotchi_raspi` service automates the process of enabling monitor mode, running `bettercap`, and providing a secure WebSocket for the Android app.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/HereLiesAz/Pwnagotchi-on-Android.git
    ```

2.  **Navigate to the service directory:**
    ```bash
    cd Pwnagotchi-on-Android/pwnagotchi_raspi
    ```

3.  **Install Python dependencies:**
    ```bash
    sudo pip3 install -r requirements.txt
    ```

4.  **Generate SSL Certificate:**
    A self-signed SSL certificate is required for the secure WebSocket server. The following command will generate a certificate valid for one year.
    ```bash
    sudo openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes -subj "/C=US/ST=California/L=San Francisco/O=Pwnagotchi/OU=Pwnagotchi/CN=pwnagotchi.local"
    ```

5.  **Move the application to `/opt`:**
    ```bash
    sudo mv ../pwnagotchi_raspi /opt/
    ```

6.  **Install and enable the `systemd` service:**
    This will ensure the service starts automatically on boot.
    ```bash
    sudo cp /opt/pwnagotchi_raspi/pwnagotchi.service /etc/systemd/system/
    sudo systemctl enable pwnagotchi.service
    sudo systemctl start pwnagotchi.service
    ```

7.  **Verify the service is running:**
    ```bash
    sudo systemctl status pwnagotchi.service
    ```
    You should see output indicating that the service is active and running.

## 3. Configure USB Gadget Mode (`g_ether`)

Configure your Raspberry Pi to act as a USB Ethernet device. This allows your Android phone to establish a network connection with it over a standard USB cable.

1.  Add `dtoverlay=dwc2` to the bottom of `/boot/config.txt`.
    ```bash
    echo "dtoverlay=dwc2" | sudo tee -a /boot/config.txt
    ```
2.  Add `g_ether` to `/etc/modules`.
    ```bash
    echo "g_ether" | sudo tee -a /etc/modules
    ```

## 4. Set a Static IP Address

Configure a static IP address for the `usb0` interface on the Raspberry Pi. This ensures your Android app can reliably connect to it.

Create or edit the file `/etc/network/interfaces.d/usb0` and add the following content:
```
allow-hotplug usb0
iface usb0 inet static
    address 10.0.0.1
    netmask 255.255.255.0
```

## 5. Reboot and Connect

Reboot your Raspberry Pi for all changes to take effect.
```bash
sudo reboot
```

Once the Raspberry Pi has rebooted, connect it to your Android device using a USB cable. The Pwnagotchi Android app, when set to Hybrid Mode, will now be able to connect to the service running on the Raspberry Pi.
