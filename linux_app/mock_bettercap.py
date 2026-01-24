import asyncio
import websockets
import json
import random
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - [MOCK] %(message)s')

async def mock_events(websocket):
    logging.info("Client connected to Mock Bettercap")
    try:
        while True:
            # Random event generation
            event_type = random.choice(['wifi.ap.new', 'wifi.client.new', 'wifi.handshake', 'other', 'wifi.ap.new'])

            if event_type == 'wifi.ap.new':
                event = {
                    "tag": "wifi.ap.new",
                    "data": {"channel": random.randint(1, 14), "ssid": f"TestAP_{random.randint(1,100)}"}
                }
            elif event_type == 'wifi.client.new':
                event = {
                    "tag": "wifi.client.new",
                    "data": {"mac": "00:11:22:33:44:55"}
                }
            elif event_type == 'wifi.handshake':
                event = {
                    "tag": "wifi.handshake",
                    "data": {"ap_bssid": "00:00:00:00:00:00", "station": "11:11:11:11:11:11", "channel": 6}
                }
            else:
                event = {"tag": "custom.event", "data": {}}

            await websocket.send(json.dumps(event))
            logging.info(f"Sent event: {event['tag']}")

            await asyncio.sleep(random.uniform(2, 5))

    except websockets.exceptions.ConnectionClosed:
        logging.info("Client disconnected")

async def main():
    async with websockets.serve(mock_events, "127.0.0.1", 8080):
        logging.info("Mock Bettercap started on ws://127.0.0.1:8080")
        await asyncio.Future()  # Run forever

if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        pass
