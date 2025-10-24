package com.hereliesaz.pwnagotchiOnAndroid

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.jsoup.Jsoup

const val OPWNGRID_LEADERBOARD_URL = "https://opwngrid.xyz/leaderboard"

class OpwngridClient {
    private val client = HttpClient(Android)

    suspend fun getLeaderboard(): List<Pair<String, Int>> {
        val response: HttpResponse = client.get(OPWNGRID_LEADERBOARD_URL)
        val html = response.bodyAsText()
        val doc = Jsoup.parse(html)
        val rows = doc.select("tr")
        return rows.mapNotNull { row ->
            val columns = row.select("td")
            if (columns.size >= 2) {
                val name = columns[0].text()
                val pwned = columns[1].text().toIntOrNull() ?: 0
                Pair(name, pwned)
            } else {
                null
            }
        }
    }
}
