import sys
import asyncio
import threading
import argparse
import signal
import logging
import subprocess
import os

from PyQt6.QtWidgets import QApplication
# Handle imports depending on how script is run
try:
    from .gui import PwnagotchiWindow
    from .service import PwnagotchiService
    from .server import AndroidServer
except ImportError:
    from linux_app.gui import PwnagotchiWindow
    from linux_app.service import PwnagotchiService
    from linux_app.server import AndroidServer

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def run_async_loop(loop, service, server):
    asyncio.set_event_loop(loop)
    try:
        loop.run_until_complete(asyncio.gather(
            service.start(),
            server.start()
        ))
        loop.run_forever()
    except Exception as e:
        logging.error(f"Async loop error: {e}")

def main():
    parser = argparse.ArgumentParser(description="Pwnagotchi Linux App")
    parser.add_argument("--mock", action="store_true", help="Run with mock Bettercap")
    args = parser.parse_args()

    app = QApplication(sys.argv)
    window = PwnagotchiWindow()

    service = PwnagotchiService()
    server = AndroidServer(service)

    # Callback to update UI and Server
    def combined_callback(state):
        # UI Update (Thread Safe Signal)
        window.update_signal.emit(state)
        # Server Update (Must be scheduled in the loop)
        try:
            loop = asyncio.get_running_loop()
            loop.create_task(server.broadcast_state(state))
        except RuntimeError:
            # If called from outside the loop (shouldn't happen with current design)
            pass

    service.update_callback = combined_callback

    # Setup Async Loop
    loop = asyncio.new_event_loop()

    # Run async loop in separate thread
    t = threading.Thread(target=run_async_loop, args=(loop, service, server), daemon=True)
    t.start()

    if args.mock:
        # Run the mock_bettercap.py script
        mock_path = os.path.join(os.path.dirname(__file__), "mock_bettercap.py")
        if os.path.exists(mock_path):
            logging.info(f"Starting Mock Bettercap from {mock_path}")
            subprocess.Popen([sys.executable, mock_path])
        else:
            logging.error("Mock Bettercap script not found!")

    window.show()

    # Handle Ctrl+C
    signal.signal(signal.SIGINT, signal.SIG_DFL)

    sys.exit(app.exec())

if __name__ == "__main__":
    main()
