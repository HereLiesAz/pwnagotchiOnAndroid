import asyncio
import logging
import os
import subprocess
import websockets
import json
import random
import time

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

import argparse

BETTERCAP_PATH = "bettercap" # rely on path
WLAN_INTERFACE = "wlan0"
WEBSOCKET_HOST = "0.0.0.0"
WEBSOCKET_PORT = 8765

bettercap_process = None
connected_clients = set()

# State
state = {
    "face": "(O_O)",
    "channel": "*",
    "aps": 0,
    "uptime": "00:00:00",
    "shakes": 0,
    "mode": "MANU",
    "epoch": 0
}

start_time = time.time()

async def broadcast_state():
    """Broadcasts the current state to all connected clients."""
    global state
    elapsed = int(time.time() - start_time)
    hours, rem = divmod(elapsed, 3600)
    minutes, seconds = divmod(rem, 60)
    state["uptime"] = "{:02}:{:02}:{:02}".format(hours, minutes, seconds)

    # Simulate face changes if no bettercap
    if not bettercap_process:
        state["face"] = "(x_x)" if state["epoch"] % 10 < 5 else "(-_-)"
        state["mode"] = "BLIND"
    else:
        # If bettercap is running, we might be AUTO
        state["mode"] = "AUTO"
        if state["epoch"] % 5 == 0:
            state["face"] = "(^o^)"
        else:
            state["face"] = "(O_O)"

    state["epoch"] += 1

    msg = {
        "type": "ui_update",
        "data": state
    }

    if connected_clients:
        await websockets.broadcast(connected_clients, json.dumps(msg))

async def loop_state_updates():
    while True:
        await broadcast_state()
        await asyncio.sleep(1)

async def start_bettercap():
    """Starts the bettercap process and enables monitor mode."""
    global bettercap_process
    logging.info("Starting bettercap...")
    try:
        # Check if we have sudo
        # For desktop app running as user, sudo might prompt or fail.
        # We try to run without sudo if possible, but bettercap needs it for packet capture.
        # Here we assume user might have set capabilities or we just fail gracefully.

        cmd = ["sudo", "-n", BETTERCAP_PATH, "-iface", WLAN_INTERFACE, "-caplet", "pwnagotchi-auto"]
        # If -n fails, we might try without sudo?

        bettercap_process = await asyncio.create_subprocess_exec(
            *cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )
        logging.info(f"Bettercap started with PID: {bettercap_process.pid}")
        return True
    except Exception as e:
        logging.error(f"Failed to start bettercap: {e}")
        return False

async def stop_bettercap():
    """Stops the bettercap process."""
    global bettercap_process
    if bettercap_process:
        logging.info("Stopping bettercap...")
        try:
            bettercap_process.terminate()
            await bettercap_process.wait()
        except:
            pass
        logging.info("Bettercap stopped.")
        bettercap_process = None

async def forward_bettercap_output():
    """Reads bettercap's stdout and forwards it."""
    while True:
        if bettercap_process and bettercap_process.stdout:
            line = await bettercap_process.stdout.readline()
            if line:
                try:
                    # If bettercap outputs JSON events (via caplet), we might parse them to update our state
                    decoded = line.decode().strip()
                    # For now just log
                    # logging.info(f"BC: {decoded}")
                except:
                    pass
        else:
            await asyncio.sleep(1)

async def handle_client(websocket, path):
    """Handles WebSocket client connections."""
    logging.info("Client connected.")
    connected_clients.add(websocket)

    # Send initial plugin list (mock)
    await websocket.send(json.dumps({
        "type": "plugin_list",
        "data": [
            {"name": "grid", "enabled": True},
            {"name": "logtail", "enabled": True}
        ]
    }))

    try:
        await websocket.wait_closed()
    finally:
        logging.info("Client disconnected.")
        connected_clients.remove(websocket)

async def main():
    """Main function to start the server."""
    global WLAN_INTERFACE

    parser = argparse.ArgumentParser()
    parser.add_argument("--iface", default="wlan0", help="Wireless interface to use")
    args = parser.parse_args()
    WLAN_INTERFACE = args.iface

    logging.info("Starting Pwnagotchi Desktop Backend...")

    # Try to start bettercap, but don't fail if it fails
    if await start_bettercap():
        logging.info("Bettercap running.")
        forwarder = asyncio.create_task(forward_bettercap_output())
    else:
        logging.warning("Bettercap failed to start (Root/Sudo required?). Running in Blind mode.")

    # Start WebSocket server (No SSL for local desktop app)
    async with websockets.serve(handle_client, WEBSOCKET_HOST, WEBSOCKET_PORT):
        logging.info(f"WebSocket server started on ws://{WEBSOCKET_HOST}:{WEBSOCKET_PORT}")

        # Start state loop
        updater = asyncio.create_task(loop_state_updates())

        try:
            await asyncio.Future() # Run forever
        finally:
            updater.cancel()
            await stop_bettercap()

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logging.info("Shutting down...")
