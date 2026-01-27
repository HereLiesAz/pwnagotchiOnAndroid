import pytest
import asyncio
import json
import os
from unittest.mock import MagicMock, AsyncMock, patch
from linux_app.service import PwnagotchiService
from linux_app.server import AndroidServer
from linux_app.plugin_manager import PluginManager

@pytest.fixture
def mock_plugin_manager(tmp_path):
    # Setup dummy plugins dir
    plugins_dir = tmp_path / "plugins"
    plugins_dir.mkdir()
    state_file = tmp_path / "plugins.json"

    # Create a dummy plugin
    (plugins_dir / "test_plugin.py").write_text("# Test Plugin")

    pm = PluginManager(plugins_dir=str(plugins_dir), state_file=str(state_file))
    return pm

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

    with patch("linux_app.server.broadcast") as mock_broadcast:
        await server.broadcast_state(state)
        mock_broadcast.assert_called_once()
        args, _ = mock_broadcast.call_args

        # Verify message content
        sent_message = json.loads(args[1])
        assert sent_message["type"] == "ui_update"
        assert sent_message["data"]["face"] == "happy"
        assert sent_message["data"]["aps"] == 10

@pytest.mark.asyncio
async def test_server_list_plugins(mock_plugin_manager):
    service = PwnagotchiService()
    server = AndroidServer(service)
    server.plugin_manager = mock_plugin_manager # Inject mock manager

    mock_ws = AsyncMock()
    # Mock incoming messages
    mock_ws.__aiter__.return_value = [json.dumps({"command": "list_plugins"})]

    await server.handle_client(mock_ws)

    # Verify response
    # Expected calls: 1. send state, 2. send plugin list
    assert mock_ws.send.call_count == 2

    call_args_list = mock_ws.send.call_args_list
    plugin_response = json.loads(call_args_list[1][0][0])

    assert plugin_response["type"] == "plugin_list"
    assert len(plugin_response["data"]) == 1
    assert plugin_response["data"][0]["name"] == "test_plugin"
    assert plugin_response["data"][0]["enabled"] == False

@pytest.mark.asyncio
async def test_server_toggle_plugin(mock_plugin_manager):
    service = PwnagotchiService()
    server = AndroidServer(service)
    server.plugin_manager = mock_plugin_manager

    mock_ws = AsyncMock()
    mock_ws.__aiter__.return_value = [json.dumps({
        "command": "toggle_plugin",
        "plugin_name": "test_plugin",
        "enabled": True
    })]

    await server.handle_client(mock_ws)

    # Check if state was updated in manager
    plugins = mock_plugin_manager.get_plugins()
    assert plugins[0]["enabled"] == True

    # Verify response contains updated list
    call_args_list = mock_ws.send.call_args_list
    plugin_response = json.loads(call_args_list[1][0][0])
    assert plugin_response["type"] == "plugin_list"
    assert plugin_response["data"][0]["enabled"] == True
