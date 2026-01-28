import os
import json
import logging
import aiohttp

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s')


class PluginManager:
    def __init__(self, plugins_dir="linux_app/plugins",
                 state_file="linux_app/plugins.json"):
        self.plugins_dir = plugins_dir
        self.state_file = state_file
        self.plugins_state = self._load_state()

        if not os.path.exists(self.plugins_dir):
            os.makedirs(self.plugins_dir)

    def _load_state(self):
        if os.path.exists(self.state_file):
            try:
                with open(self.state_file, 'r') as f:
                    return json.load(f)
            except json.JSONDecodeError:
                return {}
        return {}

    def _save_state(self):
        with open(self.state_file, 'w') as f:
            json.dump(self.plugins_state, f)

    def get_plugins(self):
        """
        Lists local plugins and their enabled state.
        Returns a list of dicts: [{'name': '...', 'enabled': bool}]
        """
        plugins = []
        if os.path.exists(self.plugins_dir):
            for filename in os.listdir(self.plugins_dir):
                if filename.endswith(".py"):
                    name = filename[:-3]
                    enabled = self.plugins_state.get(name, False)
                    plugins.append({"name": name, "enabled": enabled})
        return plugins

    def toggle_plugin(self, name, enabled):
        """
        Updates the enabled state of a plugin.
        """
        safe_name = os.path.basename(name)
        if os.path.exists(os.path.join(self.plugins_dir, f"{safe_name}.py")):
            self.plugins_state[safe_name] = enabled
            self._save_state()
            logging.info(f"Plugin {safe_name} toggled to {enabled}")
            return True
        return False

    async def get_community_plugins(self):
        """
        Fetches community plugins from the official repository.
        Returns a list of dicts: [{'name': '...', 'description': '...'}]
        """
        url = ("https://api.github.com/repos/pwnagotchi-plugins-contrib/"
               "pwnagotchi-plugins/contents/")
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(url) as response:
                    if response.status == 200:
                        data = await response.json()
                        plugins = []
                        for item in data:
                            if (item['type'] == 'dir' and
                                    not item['name'].startswith('.')):
                                plugins.append({
                                    "name": item['name'],
                                    "description": "Community Plugin"
                                })
                        return plugins
                    else:
                        logging.error(
                            f"Failed to fetch community plugins: "
                            f"{response.status}")
                        return []
        except Exception as e:
            logging.error(f"Error fetching community plugins: {e}")
            return []

    async def install_plugin(self, name):
        """
        Installs a community plugin by downloading it from the repo.
        """
        safe_name = os.path.basename(name)
        # Construct the raw content URL.
        # Assuming the standard structure where the plugin file is inside a
        # directory with the same name, or the file itself is name.py.
        # The 'get_community_plugins' lists directories.
        # Often plugins are: repo/plugin_name/plugin_name.py or just
        # repo/plugin_name.py
        # Based on pwnagotchi-plugins-contrib, it's usually a folder per
        # plugin.

        # We will try to fetch the file from the directory.
        base_url = ("https://raw.githubusercontent.com/"
                    "pwnagotchi-plugins-contrib/pwnagotchi-plugins/master")
        file_url = f"{base_url}/{safe_name}/{safe_name}.py"

        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(file_url) as response:
                    if response.status == 200:
                        content = await response.text()
                        file_path = os.path.join(
                            self.plugins_dir, f"{safe_name}.py")
                        with open(file_path, 'w') as f:
                            f.write(content)
                        logging.info(
                            f"Plugin {safe_name} installed from {file_url}")
                        return True
                    else:
                        # Try alternative: maybe it's just a file in the root?
                        # But get_community_plugins filtered for 'dir'.
                        logging.error(
                            f"Failed to download plugin {safe_name}: "
                            f"{response.status}")
                        return False
        except Exception as e:
            logging.error(f"Failed to install plugin {safe_name}: {e}")
            return False
