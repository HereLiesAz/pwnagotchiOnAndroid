import pytest
import asyncio
from unittest.mock import MagicMock, AsyncMock, patch
from linux_app.service import PwnagotchiService

@pytest.mark.asyncio
async def test_service_initial_state():
    service = PwnagotchiService()
    assert service.state["face"] == "neutral"
    assert service.state["aps"] == 0
    assert service.state["shakes"] == 0

@pytest.mark.asyncio
async def test_process_bettercap_event_ap():
    service = PwnagotchiService()
    event = {
        "tag": "wifi.ap.new",
        "data": {"channel": 6}
    }
    await service.process_bettercap_event(event)
    assert service.state["aps"] == 1
    assert service.state["face"] == "observing"
    assert service.state["channel"] == "6"

@pytest.mark.asyncio
async def test_process_bettercap_event_handshake():
    service = PwnagotchiService()
    event = {
        "tag": "wifi.handshake",
        "data": {"station": "00:11:22:33:44:55", "channel": 11}
    }
    await service.process_bettercap_event(event)
    assert service.state["shakes"] == 1
    assert service.state["face"] == "cool"
    assert service.state["channel"] == "11"
    assert len(service.state["recent_handshakes"]) == 1

@pytest.mark.asyncio
async def test_update_callback():
    mock_callback = MagicMock()
    service = PwnagotchiService(update_callback=mock_callback)

    event = {"tag": "wifi.ap.new", "data": {}}
    await service.process_bettercap_event(event)

    mock_callback.assert_called()
    args, _ = mock_callback.call_args
    assert args[0]["aps"] == 1
