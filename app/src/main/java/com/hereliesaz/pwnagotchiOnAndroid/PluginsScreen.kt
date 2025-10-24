package com.hereliesaz.pwnagotchiOnAndroid

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginsScreen(
    plugins: List<Plugin>,
    communityPlugins: List<CommunityPlugin>,
    onTogglePlugin: (String, Boolean) -> Unit,
    onInstallPlugin: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(stringResource(id = R.string.installed), stringResource(id = R.string.discover))
    var searchQuery by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.plugins)) }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Plugins") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            when (selectedTab) {
                0 -> {
                    val filteredPlugins = plugins.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    InstalledPluginsScreen(plugins = filteredPlugins, onTogglePlugin = onTogglePlugin)
                }
                1 -> {
                    val filteredCommunityPlugins = communityPlugins.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    DiscoverPluginsScreen(plugins = filteredCommunityPlugins, onInstallPlugin = onInstallPlugin)
                }
            }
        }
    }
}
