import unittest
from unittest.mock import MagicMock, patch
import asyncio
import sys
import os

# Add the directory containing main.py to the python path
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# Mock dependencies that might not be installed in the test environment
sys.modules['websockets'] = MagicMock()

# Now we can import main
import main

class TestPwnagotchiRaspi(unittest.TestCase):

    @patch('main.start_bettercap')
    def test_startup(self, mock_start_bettercap):
        # This is a basic test to ensure the module can be imported and constants are defined
        self.assertEqual(main.WLAN_INTERFACE, "wlan0")
        self.assertEqual(main.WEBSOCKET_PORT, 8765)

    def test_global_vars(self):
        self.assertIsNone(main.bettercap_process)
        self.assertEqual(len(main.connected_clients), 0)

if __name__ == '__main__':
    unittest.main()
