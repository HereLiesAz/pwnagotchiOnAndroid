import sys
import asyncio
import threading
import signal
import logging

from PyQt6.QtWidgets import QApplication

from .gui import PwnagotchiWindow
from .service import PwnagotchiService
from .server import AndroidServer

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s')

logger = logging.getLogger(__name__)


def run_async_loop(loop, service, server):
    """
    Runs the asyncio event loop in a separate thread.
    """
    asyncio.set_event_loop(loop)
    try:
        loop.run_until_complete(asyncio.gather(
            service.start(),
            server.start()
        ))
        loop.run_forever()
    except Exception as e:
        logger.error(f"Async loop error: {e}")


def main():
    """
    Main entry point for the Pwnagotchi Linux App.
    """
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
            # If called from outside the loop (shouldn't happen with current
            # design)
            pass

    service.update_callback = combined_callback

    # Setup Async Loop
    loop = asyncio.new_event_loop()

    # Run async loop in separate thread
    t = threading.Thread(
        target=run_async_loop,
        args=(
            loop,
            service,
            server),
        daemon=True)
    t.start()

    window.show()

    # Handle Ctrl+C
    signal.signal(signal.SIGINT, signal.SIG_DFL)

    sys.exit(app.exec())


if __name__ == "__main__":
    main()
