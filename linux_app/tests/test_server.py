import pytest
import asyncio
import json
from unittest.mock import MagicMock, AsyncMock, patch
from linux_app.service import PwnagotchiService
from linux_app.server import AndroidServer

@pytest.mark.asyncio
async def test_server_broadcast():
    service = PwnagotchiService()
    server = AndroidServer(service)

    # Mock WebSocket client
    mock_ws = AsyncMock()
    server.connected_clients.add(mock_ws)

    state = {
        "face": "happy",
        "channel": "1",
        "aps": 10,
        "uptime": "00:00:10",
        "shakes": 5,
        "mode": "AI"
    }

    with patch("websockets.broadcast") as mock_broadcast:
        await server.broadcast_state(state)
        mock_broadcast.assert_called_once()
        args, _ = mock_broadcast.call_args

        # Verify message content
        sent_message = json.loads(args[1])
        assert sent_message["type"] == "ui_update"
        assert sent_message["data"]["face"] == "happy"
        assert sent_message["data"]["aps"] == 10

@pytest.mark.asyncio
async def test_server_handle_client_initial_state():
    service = PwnagotchiService()
    service.state["face"] = "sad"
    server = AndroidServer(service)

    mock_ws = AsyncMock()
    # Simulate client closing immediately after connecting to avoid infinite loop in async for
    mock_ws.__aiter__.return_value = []

    await server.handle_client(mock_ws)

    # Check if initial state was sent
    mock_ws.send.assert_called()
    call_args = mock_ws.send.call_args[0][0]
    sent_data = json.loads(call_args)
    assert sent_data["data"]["face"] == "sad"
