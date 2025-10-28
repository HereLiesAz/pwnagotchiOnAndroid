package com.hereliesaz.pwnagotchiOnAndroid.utils

const val bettercapInstructions = """
    This app requires bettercap to be installed on your rooted device.

    1. Install Termux from F-Droid.
    2. Open Termux and run the following commands:

    apt update
    termux-setup-storage
    pkg install root-repo
    pkg install golang git libpcap libusb
    pkg install pkg-config
    pkg install tsu
    go install github.com/bettercap/bettercap@latest && cd ${'$'}HOME/go/bin
    tsu -c ./bettercap

    3. Grant Termux superuser rights when prompted.
"""

const val busyboxInstructions = """
    This app requires Busybox to be installed on your rooted device.
    The recommended way to install it is through a Magisk module.

    1. Open the Magisk Manager app.
    2. Go to the 'Modules' section.
    3. Search for 'Busybox' and install the one by 'osm0sis'.
    4. Reboot your device.
"""
