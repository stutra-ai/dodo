package com.javtitful

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import kotlin.text.Regex
import android.util.Log
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
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
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7",
        "Accept-Language" to "en-US,en;q=0.9",
        "Referer" to "$mainUrl/",
        "Cache-Control" to "max-age=0",
        "Upgrade-Insecure-Requests" to "1"
    )

    override val mainPage = mainPageOf(
        mainUrl to "Home",
        "$mainUrl/most-watched-rank" to "Most Watched",
        "$mainUrl/category/jav-uncensored" to "Uncensored",
        "$mainUrl/category/amateur" to "Amateur",
        "$mainUrl/category/idol" to "Idol",
        "$mainUrl/category/english-subbed" to "English Subbed",
        "$mainUrl/tag/married-woman" to "Married",
        "$mainUrl/tag/mature-woman" to "Mature",
        "$mainUrl/tag/big-tits" to "Big Tits",
        "$mainUrl/tag/stepmother" to "Stepmother",
        "$mainUrl/tag/incest" to "Incest",
        "$mainUrl/tag/bukkake" to "Bukkake",
        "$mainUrl/tag/slut" to "Slut",
        "$mainUrl/tag/cowgirl" to "Cowgirl",
        "$mainUrl/tag/nasty" to "Nasty",
        "$mainUrl/tag/hardcore" to "Hardcore",
        "$mainUrl/tag/abuse" to "Abuse",
        "$mainUrl/tag/gal" to "Gal",
        "$mainUrl/tag/black-actor" to "Black",
        "$mainUrl/tag/pantyhose" to "Pantyhose",
        "$mainUrl/tag/prostitutes" to "Prostitutes",
        "$mainUrl/tag/bride" to "Bride",
        "$mainUrl/tag/maid" to "Maid",
        "$mainUrl/tag/gangbang" to "Gangbang",
        "$mainUrl/tag/underwear" to "Underwear"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page == 1) {
            "${request.data}/"
        } else {
            "${request.data.removeSuffix("/")}/page/$page/"
        }

        Log.d("Cloudstream", "MainPage URL: $url")

        val document = app.get(url, headers = mainHeaders).document
        val items = document.select("div.video-item, div.item, article, div.thumbnail")

        val home = items.mapNotNull { it.toSearchResponse() }

        val hasNext = home.isNotEmpty()

        return newHomePageResponse(
            list = HomePageList(
                name = request.name,
                list = home,
                isHorizontalImages = true
            ),
            hasNext = hasNext
        )
    }

    private fun Element.toSearchResponse(): SearchResponse? {
        val linkElement = this.selectFirst("a")
        val href = fixUrlNull(linkElement?.attr("href")) ?: return null

        val imgElement = this.selectFirst("img")
        val title = imgElement?.attr("alt")?.trim()?.ifBlank { null }
            ?: linkElement?.attr("title")?.trim()?.ifBlank { null }
            ?: linkElement?.text()?.trim()?.ifBlank { null }
            ?: this.selectFirst(".title")?.text()?.trim()
            ?: this.selectFirst("h2")?.text()?.trim()
            ?: return null

        if (title.contains("Advertisement", ignoreCase = true)) return null

        val posterUrl = fixUrlNull(imgElement?.attr("src") ?: imgElement?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl
            this.posterHeaders = mainHeaders
        }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val url = "$mainUrl/page/$page/?s=$query"

        val document = app.get(url, headers = mainHeaders).document
        val items = document.select("div.video-item, div.item, article")

        val results = items.mapNotNull { it.toSearchResponse() }
        val hasNext = results.isNotEmpty()

        return newSearchResponseList(results, hasNext = hasNext)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query)

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url, headers = mainHeaders).document

        val title = document.selectFirst("h1.title, h1.entry-title, h1")?.text()?.trim()
            ?: document.selectFirst(".video-title")?.text()?.trim()
            ?: "Unknown"

        val poster = fixUrlNull(document.selectFirst(".video-poster img, .thumbnail img, .poster img")?.attr("src"))

        val description = document.select(".description p, .entry-content p, .video-description p")
            .joinToString(" ") { it.text() }
            .ifBlank { "Adult content" }

        val yearText = document.selectFirst(".year, .date, .release-date")?.text()
            ?.substringBefore("-")?.toIntOrNull()

        val tags = document.select(".tags a, .categories a, .genre a")
            .mapNotNull { it.text().trim() }

        val recommendations = document.select(".related-videos a, .recommendations a, .suggestions a")
            .mapNotNull { it.toRecommendationResult() }

        val actors = document.select(".actors a, .cast a, .starring a")
            .mapNotNull { Actor(it.text()) }

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.posterHeaders = mainHeaders
            this.plot = description
            this.year = yearText
            this.tags = tags
            this.recommendations = recommendations
            addActors(actors)
        }
    }

    private fun Element.toRecommendationResult(): SearchResponse? {
        val title = this.selectFirst("img")?.attr("alt")?.trim()
        if (title.isNullOrBlank()) return null

        val href = fixUrlNull(this.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src"))

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl
            this.posterHeaders = mainHeaders
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

        // Look for video sources in the page
        val doc = Jsoup.parse(document)
        
        // Try to find iframe embed links
        val iframeRegex = Regex("""<iframe[^>]+src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val iframeMatches = iframeRegex.findAll(document).toList()
        
        // Also try to find direct video links
        val videoRegex = Regex("""file:\s*["']([^"']+\.m3u8[^"']*)["']""", RegexOption.IGNORE_CASE)
        val videoMatches = videoRegex.findAll(document).toList()

        val processedUrls = mutableSetOf<String>()

        // Process iframe sources
        for ((index, match) in iframeMatches.withIndex()) {
            try {
                val src = match.groupValues[1]
                if (!processedUrls.contains(src)) {
                    processedUrls.add(src)
                    
                    // Try to extract video from iframe
                    loadExtractor(src, data, subtitleCallback, callback)
                }
            } catch (e: Exception) {
                Log.d("kraptor_$name", "[$index] Hata: ${e.message}")
                continue
            }
        }

        // Process direct video links
        for ((index, match) in videoMatches.withIndex()) {
            try {
                val videoUrl = match.groupValues[1]
                if (!processedUrls.contains(videoUrl)) {
                    processedUrls.add(videoUrl)
                    
                    callback.invoke(
                        newExtractorLink(
                            source = "$name Direct",
                            name = "Direct $index",
                            url = videoUrl,
                            type = ExtractorLinkType.M3U8
                        ) {
                            this.referer = data
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
            } catch (e: Exception) {
                Log.d("kraptor_$name", "[$index] Video Hata: ${e.message}")
                continue
            }
        }

        // If no sources found, try to find packed JavaScript
        if (processedUrls.isEmpty()) {
            val packedRegex = Regex("""eval(function\(p,a,c,k,e,d\)""")
            if (packedRegex.containsMatchIn(document)) {
                // Find and unpack JavaScript if needed
                val scriptMatches = doc.select("script").map { it.data() }
                for (script in scriptMatches) {
                    if (script.contains("eval(function(p,a,c,k,e,d)")) {
                        try {
                            val unpacked = JsUnpacker(script).unpack()
                            if (unpacked != null) {
                                val m3u8Regex = Regex("""(https?://[^"'\s]+\.m3u8[^"'\s]*)""")
                                val m3u8Match = m3u8Regex.find(unpacked)
                                if (m3u8Match != null) {
                                    val url = m3u8Match.groupValues[1]
                                    if (!processedUrls.contains(url)) {
                                        processedUrls.add(url)
                                        callback.invoke(
                                            newExtractorLink(
                                                source = name,
                                                name = "Unpacked",
                                                url = url,
                                                type = ExtractorLinkType.M3U8
                                            ) {
                                                this.referer = data
                                                this.quality = Qualities.Unknown.value
                                            }
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("kraptor_$name", "Unpack error: ${e.message}")
                        }
                    }
                }
            }
        }

        return true
    }
}