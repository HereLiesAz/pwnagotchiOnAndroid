import asyncio
import logging
import os
import ssl
import subprocess
import websockets
import json

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

BETTERCAP_PATH = "/usr/local/bin/bettercap"
WLAN_INTERFACE = "wlan0"
CERT_FILE = "cert.pem"
KEY_FILE = "key.pem"
WEBSOCKET_HOST = "0.0.0.0"
WEBSOCKET_PORT = 8765

bettercap_process = None
connected_clients = set()

async def start_bettercap():
    """Starts the bettercap process and enables monitor mode."""
    global bettercap_process
    logging.info("Starting bettercap...")
    try:
        # Enable monitor mode
        subprocess.run(["sudo", "ip", "link", "set", WLAN_INTERFACE, "down"], check=True)
        subprocess.run(["sudo", "iw", "dev", WLAN_INTERFACE, "set", "type", "monitor"], check=True)
        subprocess.run(["sudo", "ip", "link", "set", WLAN_INTERFACE, "up"], check=True)
        logging.info("Monitor mode enabled.")

        # Start bettercap with the pwnagotchi-auto caplet
        bettercap_process = await asyncio.create_subprocess_exec(
            "sudo", BETTERCAP_PATH, "-iface", WLAN_INTERFACE, "-caplet", "pwnagotchi-auto",
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )
        logging.info(f"Bettercap started with PID: {bettercap_process.pid}")
        return True
    except (subprocess.CalledProcessError, FileNotFoundError) as e:
        logging.error(f"Failed to start bettercap: {e}")
        return False

async def stop_bettercap():
    """Stops the bettercap process and disables monitor mode."""
    global bettercap_process
    if bettercap_process:
        logging.info("Stopping bettercap...")
        bettercap_process.terminate()
        await bettercap_process.wait()
        logging.info("Bettercap stopped.")
        bettercap_process = None

    logging.info("Disabling monitor mode...")
    try:
        subprocess.run(["sudo", "ip", "link", "set", WLAN_INTERFACE, "down"], check=True)
        subprocess.run(["sudo", "iw", "dev", WLAN_INTERFACE, "set", "type", "managed"], check=True)
        subprocess.run(["sudo", "ip", "link", "set", WLAN_INTERFACE, "up"], check=True)
        logging.info("Monitor mode disabled.")
    except subprocess.CalledProcessError as e:
        logging.error(f"Failed to disable monitor mode: {e}")

async def forward_bettercap_output():
    """Reads bettercap's stdout and forwards it to connected clients."""
    while True:
        if bettercap_process and bettercap_process.stdout:
            line = await bettercap_process.stdout.readline()
            if line:
                try:
                    # Validate that the line is valid JSON
                    json.loads(line)
                    websockets.broadcast(connected_clients, line)
                except json.JSONDecodeError:
                    logging.warning(f"Skipping non-JSON line from bettercap: {line.decode().strip()}")
        else:
            await asyncio.sleep(1)


async def handle_client(websocket, path):
    """Handles WebSocket client connections."""
    logging.info("Client connected.")
    connected_clients.add(websocket)
    try:
        await websocket.wait_closed()
    finally:
        logging.info("Client disconnected.")
        connected_clients.remove(websocket)


async def main():
    """Main function to start the server."""
    if not os.path.exists(CERT_FILE) or not os.path.exists(KEY_FILE):
        logging.error("SSL certificate and key not found. Please generate them first.")
        return

    ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
    ssl_context.load_cert_chain(CERT_FILE, KEY_FILE)

    if await start_bettercap():
        server = await websockets.serve(
            handle_client, WEBSOCKET_HOST, WEBSOCKET_PORT, ssl=ssl_context
        )
        logging.info(f"WebSocket server started on wss://{WEBSOCKET_HOST}:{WEBSOCKET_PORT}")

        forwarder_task = asyncio.create_task(forward_bettercap_output())

        try:
            await server.wait_closed()
        finally:
            forwarder_task.cancel()
            await stop_bettercap()
    else:
        logging.error("Failed to start bettercap. Shutting down.")


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logging.info("Shutting down...")
