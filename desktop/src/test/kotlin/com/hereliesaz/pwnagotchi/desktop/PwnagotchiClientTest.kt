package com.hereliesaz.pwnagotchi.desktop

import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.Assert.*

class PwnagotchiClientTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testParseUiUpdate() {
        val jsonString = """{"type":"ui_update","data":{"face":"(O_O)","channel":"6","aps":10,"uptime":"00:10:00","shakes":5,"mode":"AUTO"}}"""
        val message = json.decodeFromString<BaseMessage>(jsonString)
        assertEquals("ui_update", message.type)

        val update = json.decodeFromString<UiUpdateMessage>(jsonString)
        assertEquals("(O_O)", update.data.face)
        assertEquals(10, update.data.aps)
    }

    @Test
    fun testParsePluginList() {
        val jsonString = """{"type":"plugin_list","data":[{"name":"grid","enabled":true},{"name":"logtail","enabled":false}]}"""
        val message = json.decodeFromString<BaseMessage>(jsonString)
        assertEquals("plugin_list", message.type)

        val list = json.decodeFromString<PluginListMessage>(jsonString)
        assertEquals(2, list.data.size)
        assertEquals("grid", list.data[0].name)
        assertTrue(list.data[0].enabled)
    }
}
