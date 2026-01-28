import asyncio
import json
import logging
import websockets
from collections import deque

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s')


class PwnagotchiService:
    """
    Core service logic for Pwnagotchi Linux App.
    Manages state, connection to Bettercap, and updates.
    """

    def __init__(self, config=None, update_callback=None):
        self.config = config or {}
        self.update_callback = update_callback
        self.bettercap_url = "ws://127.0.0.1:8080/api/events"
        self.running = False

        # State
        self.state = {
            "face": "neutral",
            "channel": "-",
            "aps": 0,
            "clients": 0,
            "uptime": "00:00:00",
            "shakes": 0,
            "mode": "MANU",  # MANU, AUTO, AI
            "recent_handshakes": deque(maxlen=10)
        }
        self.start_time = 0

    async def start(self):
        """
        Starts the service, connecting to Bettercap and starting the update
        loop.
        """
        self.running = True
        self.start_time = asyncio.get_running_loop().time()
        logging.info("Starting Pwnagotchi Service")
        asyncio.create_task(self.connect_bettercap())
        asyncio.create_task(self.update_loop())

    async def stop(self):
        """Stops the service."""
        self.running = False
        logging.info("Stopping Pwnagotchi Service")

    async def connect_bettercap(self):
        """Main loop for connecting to Bettercap's WebSocket."""
        while self.running:
            try:
                async with websockets.connect(self.bettercap_url) as websocket:
                    logging.info("Connected to Bettercap")
                    self.state["face"] = "happy"
                    self.notify_update()
                    async for message in websocket:
                        if not self.running:
                            break
                        try:
                            await self.process_bettercap_event(
                                json.loads(message))
                        except json.JSONDecodeError:
                            pass
            except (websockets.exceptions.ConnectionClosed,
                    ConnectionRefusedError, OSError):
                if self.running:
                    logging.warning(
                        "Bettercap connection lost/failed. "
                        "Retrying in 5s...")
                    self.state["face"] = "sad"
                    self.notify_update()
                    await asyncio.sleep(5)

    async def process_bettercap_event(self, event):
        """Processes an event received from Bettercap."""
        tag = event.get('tag', '')
        data = event.get('data', {})

        if tag == 'wifi.ap.new':
            self.state["aps"] += 1
            self.state["face"] = "observing"
        elif tag == 'wifi.client.new':
            self.state["clients"] += 1
            self.state["face"] = "observing"
        elif tag == 'wifi.handshake':
            self.state["shakes"] += 1
            self.state["face"] = "cool"
            self.state["recent_handshakes"].append(data)
            logging.info(f"Handshake captured! {data}")

        # Update channel if available
        if 'channel' in data:
            self.state["channel"] = str(data['channel'])

        self.notify_update()

    async def update_loop(self):
        """Periodic loop to update uptime and other time-based stats."""
        while self.running:
            # Update uptime
            try:
                now = asyncio.get_running_loop().time()
                elapsed = int(now - self.start_time)
                hours, remainder = divmod(elapsed, 3600)
                minutes, seconds = divmod(remainder, 60)
                self.state["uptime"] = f"{hours:02}:{minutes:02}:{seconds:02}"

                self.notify_update()
            except Exception as e:
                logging.error(f"Error in update loop: {e}")
            await asyncio.sleep(1)

    def notify_update(self):
        """Calls the update callback with a copy of the current state."""
        if self.update_callback:
            # Pass a copy of the state to avoid race conditions if callback
            # modifies it
            self.update_callback(self.state.copy())
