package com.javtitful

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import android.util.Base64
import android.util.Log
import org.jsoup.Jsoup

class JavTitful : MainAPI() {
    override var mainUrl = "https://javtitful.com"
    override var name = "J@vTitfu!"
    override val hasMainPage = true
    override var lang = "en"
    override val hasQuickSearch = false
    override val supportedTypes = setOf(TvType.NSFW)

    private val mainHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
        "Referer" to "$mainUrl/"
    )

    override val mainPage = mainPageOf(
        "$mainUrl/" to "Home",
        "$mainUrl/most-viewed/" to "Most Viewed",
        "$mainUrl/uncensored/" to "Uncensored",
        "$mainUrl/censored/" to "Censored",
        "$mainUrl/reducing-mosaic/" to "Reducing Mosaic"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page == 1) {
            "${request.data}"
        } else {
            "${request.data.removeSuffix("/")}/page/$page/"
        }

        val document = app.get(url, headers = mainHeaders).document
        val items = document.select("div.inside-article, article, .item, .video-block, li")

        val home = items.mapNotNull { it.toSearchResponse() }

        return newHomePageResponse(
            list = HomePageList(
                name = request.name,
                list = home,
                isHorizontalImages = true
            ),
            hasNext = home.isNotEmpty()
        )
    }

    private fun Element.toSearchResponse(): SearchResponse? {
        val linkElement = selectFirst("a")
        val href = fixUrlNull(linkElement?.attr("href")) ?: return null

        val imgElement = selectFirst("img")
        val title = imgElement?.attr("alt")?.trim()?.ifBlank { null }
            ?: linkElement?.attr("title")?.trim()
            ?: linkElement?.text()?.trim()
            ?: selectFirst("h2")?.text()?.trim()
            ?: return null

        val posterUrl = fixUrlNull(imgElement?.attr("src") ?: imgElement?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl
            this.posterHeaders = mainHeaders
        }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val url = "$mainUrl/page/$page/?s=$query"
        val document = app.get(url, headers = mainHeaders).document
        val items = document.select("div.inside-article, article, .item")

        val results = items.mapNotNull { it.toSearchResponse() }
        return newSearchResponseList(results, hasNext = results.isNotEmpty())
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url, headers = mainHeaders).document

        val title = document.selectFirst("h1")?.text()?.trim() ?: "Unknown"
        val poster = fixUrlNull(document.selectFirst("img.large, .screenshot img")?.attr("src"))

        val description = document.select("p, .description").joinToString(" ") { it.text() }.ifBlank { "JAV Video" }

        val actors = document.select("a[href*='actress'], .actor").mapNotNull { Actor(it.text().trim()) }

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.posterHeaders = mainHeaders
            this.plot = description
            addActors(actors)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val res = app.get(data, headers = mainHeaders)
        val document = res.text
        val doc = Jsoup.parse(document)

        // Button + iframe logic similar to JavGuru
        val buttonNames = mutableListOf<String>()
        doc.select("a.wp-btn-iframe__shortcode, button").forEach {
            buttonNames.add(it.text().trim())
        }

        val iframeRegex = Regex("\"iframe_url\":\"([^\"]*)\"")
        val iframeMatches = iframeRegex.findAll(document).toList()

        for ((index, match) in iframeMatches.withIndex()) {
            try {
                val sourceName = if (index < buttonNames.size) buttonNames[index] else "Source ${index + 1}"
                val encodedUrl = match.groupValues[1]
                val decodedUrl = base64Decode(encodedUrl)

                loadExtractor(decodedUrl, data, subtitleCallback, callback)
            } catch (e: Exception) {
                Log.d("JavTitful", "Error: ${e.message}")
            }
        }

        // Fallback direct extractors
        loadExtractor(data, data, subtitleCallback, callback)

        return true
    }
}