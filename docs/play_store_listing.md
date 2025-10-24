# Google Play Store Listing

## App Title
Pwnagotchi on Android

## Short Description
An Android client for the Pwnagotchi.

## Full Description
This is an Android client for the Pwnagotchi, a device that uses a Raspberry Pi Zero W to passively sniff Wi-Fi networks and capture handshakes. This app allows you to monitor your Pwnagotchi's activity, view captured handshakes, and manage plugins.

This app supports three modes of operation:
* **Remote Mode:** Connect to your Pwnagotchi over the network.
* **Local Mode:** Run the Pwnagotchi agent directly on your rooted Android device.
* **Hybrid Mode:** Use your Android device as the "brain" for a Raspberry Pi connected via USB.

## What's New
* Implemented Hybrid Mode, allowing you to use your phone as the "brain" for a USB-connected Raspberry Pi.
* Refactored the data sources to reduce code duplication and improve maintainability.
* Added a mode selector to the settings screen to allow you to choose between Remote, Local, and Hybrid modes.
* Disabled unencrypted backups for improved security.
* Optimized the performance of the handshake list.
* Cleaned up the codebase by removing unnecessary TODO comments.
