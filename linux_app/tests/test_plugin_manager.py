import pytest
import os
import json
from unittest.mock import AsyncMock, patch, MagicMock
from linux_app.plugin_manager import PluginManager


@pytest.fixture
def plugin_manager(tmp_path):
    plugins_dir = tmp_path / "plugins"
    state_file = tmp_path / "plugins.json"
    return PluginManager(plugins_dir=str(plugins_dir),
                         state_file=str(state_file))


def test_plugin_manager_get_plugins(plugin_manager):
    # Create a dummy plugin file
    with open(os.path.join(plugin_manager.plugins_dir, "test.py"), "w") as f:
        f.write("pass")

    plugins = plugin_manager.get_plugins()
    assert len(plugins) == 1
    assert plugins[0]["name"] == "test"
    assert plugins[0]["enabled"] is False  # Default


def test_plugin_manager_toggle(plugin_manager):
    with open(os.path.join(plugin_manager.plugins_dir, "test.py"), "w") as f:
        f.write("pass")

    assert plugin_manager.toggle_plugin("test", True)

    # Verify persistence
    with open(plugin_manager.state_file, "r") as f:
        state = json.load(f)
        assert state["test"] is True

    plugins = plugin_manager.get_plugins()
    assert plugins[0]["enabled"] is True


@pytest.mark.asyncio
async def test_get_community_plugins_success(plugin_manager):
    mock_response_data = [
        {"name": "plugin1", "type": "dir"},
        {"name": "plugin2", "type": "dir"},
        {"name": ".hidden", "type": "dir"},
        {"name": "file.txt", "type": "file"}
    ]

    # Setup mock response to act as an async context manager
    mock_response = AsyncMock()
    mock_response.status = 200
    mock_response.json.return_value = mock_response_data

    mock_context = AsyncMock()
    mock_context.__aenter__.return_value = mock_response

    # mock_session.get() returns the context manager
    mock_session = MagicMock()
    mock_session.get.return_value = mock_context
    mock_session.__aenter__.return_value = mock_session
    mock_session.__aexit__.return_value = None

    # ClientSession() returns the session context manager
    with patch("aiohttp.ClientSession") as mock_session_cls:
        mock_session_cls.return_value = mock_session
        plugins = await plugin_manager.get_community_plugins()

    assert len(plugins) == 2
    names = [p["name"] for p in plugins]
    assert "plugin1" in names
    assert "plugin2" in names


@pytest.mark.asyncio
async def test_install_plugin_success(plugin_manager):
    mock_content = "print('installed')"

    mock_response = AsyncMock()
    mock_response.status = 200
    mock_response.text.return_value = mock_content

    mock_context = AsyncMock()
    mock_context.__aenter__.return_value = mock_response

    mock_session = MagicMock()
    mock_session.get.return_value = mock_context
    mock_session.__aenter__.return_value = mock_session
    mock_session.__aexit__.return_value = None

    with patch("aiohttp.ClientSession") as mock_session_cls:
        mock_session_cls.return_value = mock_session
        success = await plugin_manager.install_plugin("awesome_plugin")

    assert success is True
    installed_path = os.path.join(
        plugin_manager.plugins_dir,
        "awesome_plugin.py")
    assert os.path.exists(installed_path)
    with open(installed_path, "r") as f:
        assert f.read() == mock_content
