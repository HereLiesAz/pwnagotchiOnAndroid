#!/bin/bash
set -e

# Configuration
BETTERCAP_VERSION="v2.32.0" # Latest stable as of knowledge cutoff, or strictly user provided link
INSTALL_DIR="$(dirname "$0")/bin"
mkdir -p "$INSTALL_DIR"

# Detect OS and Arch
OS="$(uname -s | tr '[:upper:]' '[:lower:]')"
ARCH="$(uname -m)"

case "$ARCH" in
    x86_64)
        BC_ARCH="amd64"
        ;;
    aarch64)
        BC_ARCH="arm64"
        ;;
    armv7l)
        BC_ARCH="arm" # bettercap usually calls it arm or armv7
        ;;
    *)
        echo "Unsupported architecture: $ARCH"
        exit 1
        ;;
esac

echo "Detected $OS $ARCH. Downloading Bettercap ($BC_ARCH)..."

# Construct URL (Bettercap naming convention: bettercap_linux_amd64_v2.32.0.zip)
# Note: Newer versions might use slightly different naming, checking standard pattern.
# GitHub release pattern: https://github.com/bettercap/bettercap/releases/download/v2.32.0/bettercap_linux_amd64_v2.32.0.zip
BC_FILENAME="bettercap_${OS}_${BC_ARCH}_${BETTERCAP_VERSION}.zip"
BC_URL="https://github.com/bettercap/bettercap/releases/download/${BETTERCAP_VERSION}/${BC_FILENAME}"

echo "Downloading from $BC_URL..."
curl -L -o "$INSTALL_DIR/bettercap.zip" "$BC_URL"

echo "Extracting..."
unzip -o "$INSTALL_DIR/bettercap.zip" -d "$INSTALL_DIR"
rm "$INSTALL_DIR/bettercap.zip"
chmod +x "$INSTALL_DIR/bettercap"

echo "Bettercap installed to $INSTALL_DIR/bettercap"
echo "Done."
