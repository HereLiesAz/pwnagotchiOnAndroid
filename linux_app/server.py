import json
import logging
import ssl
import websockets
import os
import secrets
from urllib.parse import urlparse, parse_qs
try:
    from .plugin_manager import PluginManager
except ImportError:
    from linux_app.plugin_manager import PluginManager

logger = logging.getLogger(__name__)


class AndroidServer:
    """
    Secure WebSocket server for connecting the Android app to the Linux
    Pwnagotchi.
    """

    def __init__(self, service, host="0.0.0.0", port=8765, api_key=None):
        self.service = service
        self.host = host
        self.port = port
        self.connected_clients = set()
        self.server = None
        self.plugin_manager = PluginManager()
        self.api_key = api_key
        if not self.api_key:
            self.api_key = secrets.token_urlsafe(16)
            logger.warning(
                f"No API key provided. Generated temporary key: "
                f"{self.api_key}")
        else:
            logger.info(f"Server configured with API Key: {self.api_key}")

    async def start(self):
        """Starts the WebSocket server."""
        # SSL Setup
        cert_file = os.path.join(os.path.dirname(__file__), "cert.pem")
        key_file = os.path.join(os.path.dirname(__file__), "key.pem")

        ssl_context = None
        if os.path.exists(cert_file) and os.path.exists(key_file):
            ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
            ssl_context.load_cert_chain(cert_file, key_file)
            logger.info("SSL Certificates loaded.")
        else:
            logger.warning(
                "SSL Certificates not found! Running in insecure mode.")

        scheme = "wss" if ssl_context else "ws"
        logger.info(
            f"Starting Android Server on {scheme}://{self.host}:{self.port}")

        self.server = await websockets.serve(
            self.handle_client, self.host, self.port, ssl=ssl_context
        )

    async def stop(self):
        """Stops the WebSocket server."""
        if self.server:
            self.server.close()
            await self.server.wait_closed()
            logger.info("Android Server stopped.")

    async def handle_client(self, websocket):
        """Handles a new client connection with authentication."""
        # Check authentication
        # We expect the API key in the 'Authorization' header or a query param
        # 'key'
        # Note: websockets library passes path and headers in handshake,
        # but we are in the handler after handshake.
        # However, for simplicity with this library structure, we can check
        # the request headers if accessible or expect the first message to be
        # an auth packet.

        # Accessing headers in newer websockets versions:
        try:
            # Attempt to retrieve API Key from query params or headers
            # Since 'websocket' object in handler typically exposes request
            # info
            request_path = getattr(websocket, 'path', '')
            request_headers = getattr(websocket, 'request_headers', {})

            authenticated = False

            # Parse query parameters and require an exact ?key=<API_KEY> match
            if request_path:
                try:
                    parsed = urlparse(request_path)
                    query_params = parse_qs(parsed.query)
                    key_values = query_params.get("key") or []
                    if key_values and key_values[0] == self.api_key:
                        authenticated = True
                except Exception:
                    logger.exception(
                        "Failed to parse WebSocket request path for API key")

            # Fallback: parse Authorization header with expected scheme
            if not authenticated:
                auth_header = request_headers.get("Authorization")
                if auth_header:
                    # Support standard "Bearer <token>" or raw token (legacy)
                    if auth_header == self.api_key:
                        authenticated = True
                    else:
                        scheme, _, credentials = auth_header.partition(" ")
                        if (scheme.lower() == "bearer" and
                                credentials == self.api_key):
                            authenticated = True

            logger.info(
                "WebSocket authentication %s using configured API key",
                "succeeded" if authenticated else "failed",
            )

            if not authenticated:
                logger.warning(
                    f"Unauthorized connection attempt from "
                    f"{websocket.remote_address}")
                await websocket.close(code=1008, reason="Unauthorized")
                return

        except Exception as e:
            logger.error(f"Authentication check failed: {e}")
            await websocket.close(code=1011, reason="Auth Error")
            return

        logger.info(f"Android Client connected: {websocket.remote_address}")
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
                        plugins = self.plugin_manager.get_plugins()
                        response = {
                            "type": "plugin_list",
                            "data": plugins
                        }
                        await websocket.send(json.dumps(response))

                    elif command == "toggle_plugin":
                        name = data.get("plugin_name")
                        enabled = data.get("enabled")
                        if name is not None and enabled is not None:
                            self.plugin_manager.toggle_plugin(name, enabled)
                            # Refresh list for client
                            plugins = self.plugin_manager.get_plugins()
                            response = {
                                "type": "plugin_list",
                                "data": plugins
                            }
                            await websocket.send(json.dumps(response))

                    elif command == "get_community_plugins":
                        plugins = await (
                            self.plugin_manager.get_community_plugins())
                        response = {
                            "type": "community_plugin_list",
                            "data": plugins
                        }
                        await websocket.send(json.dumps(response))

                    elif command == "install_community_plugin":
                        name = data.get("plugin_name")
                        if name:
                            await self.plugin_manager.install_plugin(name)
                            # Refresh list
                            plugins = self.plugin_manager.get_plugins()
                            response = {
                                "type": "plugin_list",
                                "data": plugins
                            }
                            await websocket.send(json.dumps(response))

                except json.JSONDecodeError:
                    logger.warning(f"Received malformed JSON: {message}")
                except Exception as e:
                    logger.error(f"Error handling command: {e}")
        finally:
            self.connected_clients.remove(websocket)
            logger.info("Android Client disconnected.")

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
        websockets.broadcast(self.connected_clients, json_message)

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
