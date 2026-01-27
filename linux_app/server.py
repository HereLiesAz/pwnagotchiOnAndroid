import asyncio
import json
import logging
import ssl
import websockets
from websockets import broadcast
import os

class AndroidServer:
    """
    Secure WebSocket server for connecting the Android app to the Linux Pwnagotchi.
    """
    def __init__(self, service, host="0.0.0.0", port=8765):
        self.service = service
        self.host = host
        self.port = port
        self.connected_clients = set()
        self.server = None

    async def start(self):
        """Starts the WebSocket server."""
        # SSL Setup
        cert_file = os.path.join(os.path.dirname(__file__), "cert.pem")
        key_file = os.path.join(os.path.dirname(__file__), "key.pem")

        ssl_context = None
        if os.path.exists(cert_file) and os.path.exists(key_file):
            ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
            ssl_context.load_cert_chain(cert_file, key_file)
            logging.info("SSL Certificates loaded.")
        else:
            logging.warning("SSL Certificates not found! Running in insecure mode.")

        logging.info(f"Starting Android Server on wss://{self.host}:{self.port}")
        self.server = await websockets.serve(
            self.handle_client, self.host, self.port, ssl=ssl_context
        )

    async def stop(self):
        """Stops the WebSocket server."""
        if self.server:
            self.server.close()
            await self.server.wait_closed()
            logging.info("Android Server stopped.")

    async def handle_client(self, websocket):
        """Handles a new client connection."""
        logging.info("Android Client connected.")
        self.connected_clients.add(websocket)
        try:
            # Send initial state
            await self.send_state(websocket, self.service.state)

            async for message in websocket:
                # Handle commands from Android
                try:
                    data = json.loads(message)
                    command = data.get("command")
                    if command == "list_plugins":
                         # Mock response
                         response = {
                             "type": "plugin_list",
                             "data": []
                         }
                         await websocket.send(json.dumps(response))
                except json.JSONDecodeError:
                    pass
        finally:
            self.connected_clients.remove(websocket)
            logging.info("Android Client disconnected.")

    async def broadcast_state(self, state):
        """Broadcasts the current state to all connected clients."""
        if not self.connected_clients:
            return

        message = {
            "type": "ui_update",
            "data": {
                "face": state["face"],
                "channel": str(state["channel"]),
                "aps": state["aps"],
                "uptime": state["uptime"],
                "shakes": state["shakes"],
                "mode": state["mode"]
            }
        }
        json_message = json.dumps(message)
        # Broadcast to all connected clients
        broadcast(self.connected_clients, json_message)

    async def send_state(self, websocket, state):
        """Sends the current state to a specific client."""
        message = {
            "type": "ui_update",
            "data": {
                "face": state["face"],
                "channel": str(state["channel"]),
                "aps": state["aps"],
                "uptime": state["uptime"],
                "shakes": state["shakes"],
                "mode": state["mode"]
            }
        }
        await websocket.send(json.dumps(message))
