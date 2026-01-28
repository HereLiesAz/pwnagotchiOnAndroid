import pytest
from unittest.mock import MagicMock
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


@pytest.mark.asyncio
async def test_process_bettercap_event_client():
    service = PwnagotchiService()
    event = {
        "tag": "wifi.client.new",
        "data": {}
    }
    await service.process_bettercap_event(event)
    assert service.state["clients"] == 1
    assert service.state["face"] == "observing"


@pytest.mark.asyncio
async def test_process_bettercap_event_missing_channel():
    service = PwnagotchiService()
    service.state["channel"] = "5"

    event = {
        "tag": "wifi.ap.new",
        "data": {}  # No channel
    }
    await service.process_bettercap_event(event)
    # Channel should remain 5
    assert service.state["channel"] == "5"
    assert service.state["aps"] == 1


@pytest.mark.asyncio
async def test_recent_handshakes_maxlen():
    service = PwnagotchiService()

    # Add 11 handshakes
    for i in range(11):
        event = {
            "tag": "wifi.handshake",
            "data": {"station": f"mac_{i}"}
        }
        await service.process_bettercap_event(event)

    assert len(service.state["recent_handshakes"]) == 10
    # The oldest (mac_0) should be gone, mac_1 to mac_10 remain
    assert service.state["recent_handshakes"][0]["station"] == "mac_1"
    assert service.state["recent_handshakes"][-1]["station"] == "mac_10"


@pytest.mark.asyncio
async def test_notify_update_copy_semantics():
    mock_callback = MagicMock()
    service = PwnagotchiService(update_callback=mock_callback)

    # Trigger update
    service.notify_update()

    args, _ = mock_callback.call_args
    state_copy = args[0]

    # Mutate the copy
    state_copy["face"] = "hacked"

    # Verify original service state is unaffected
    assert service.state["face"] == "neutral"
