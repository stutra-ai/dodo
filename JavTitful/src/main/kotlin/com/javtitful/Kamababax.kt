package com.kamababax

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class Kamababax : MainAPI() {
    override var mainUrl = "https://www.kamababax.com"
    override var name = "Kamababax"
    override val supportedTypes = setOf(TvType.NSFW)
    override var lang = "hi"
    override val hasMainPage = true
    override val hasQuickSearch = true

    override val mainPage = mainPageOf(
        "$mainUrl/?filter=latest" to "Latest Videos",
        "$mainUrl/?filter=random" to "Random",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if (page <= 1) request.data else "${request.data.removeSuffix("/")}/page/$page/"
        val document = app.get(url, referer = mainUrl).document
        
        val items = document.select("article.post, .video-block, .thumb-block, .item, .post").mapNotNull { 
            it.toSearchResult() 
        }.distinctBy { it.url }

        return newHomePageResponse(request.name, items, hasNext = true)
    }

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val url = if (page <= 1) "$mainUrl/?s=$query" else "$mainUrl/page/$page/?s=$query"
        val document = app.get(url, referer = mainUrl).document
        
        val results = document.select("article.post, .video-block, .thumb-block, .item").mapNotNull { 
            it.toSearchResult() 
        }.distinctBy { it.url }

        return newSearchResponseList(results, hasNext = true)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val anchor = selectFirst("a[href*='/']") ?: return null
        val href = fixUrlNull(anchor.attr("href")) ?: return null
        
        val title = selectFirst("h2.entry-title, .title, h3, h4")?.text()?.trim()
            ?: anchor.text()?.trim() ?: return null

        val imgEl = selectFirst("img")
        var poster = imgEl?.attr("src")
            ?: imgEl?.attr("data-src")
            ?: imgEl?.attr("data-original")
            ?: selectFirst("[style*='background']")?.attr("style")?.let {
                Regex("url\\([\"']?(.*?)['\"]?\\)").find(it)?.groupValues?.get(1)
            }

        if (poster != null) {
            if (poster.startsWith("//")) {
                poster = "https:$poster"
            }
            poster = fixUrl(poster)
        }

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = poster
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url, referer = mainUrl).document
        val title = document.selectFirst("h1.entry-title, .title")?.text()?.trim() 
            ?: "Kamababax Video"

        var poster = document.selectFirst("meta[property=og:image]")?.attr("content")
            ?: document.selectFirst("img.wp-post-image, .featured-image img")?.attr("src")

        if (poster != null) {
            poster = fixUrl(poster)
        }

        val description = document.selectFirst("meta[property=og:description]")?.attr("content")
            ?: document.selectFirst(".entry-content p")?.text()

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data, referer = mainUrl).document
        val rawHtml = document.toString()
        var found = false

        // 1. Scan for direct streams
        listOf(
            Regex("""["']?(https?://[^"']+\.(?:mp4|m3u8))["']"""),
            Regex("""source\s*:\s*["']([^"']+\.(?:mp4|m3u8))["']"""),
            Regex("""file["']\s*:\s*["']([^"']+)["']""")
        ).forEach { regex ->
            regex.findAll(rawHtml).forEach { match ->
                val link = match.groupValues[1]
                if (link.contains(".mp4") || link.contains(".m3u8")) {
                    val fixedLink = fixUrl(link)
                    val isM3u8 = fixedLink.contains(".m3u8")
                    
                    // FIXED: Aligned positional arguments matching (name, source, url, type) { lambda }
                    callback(
                        newExtractorLink(
                            name,
                            if (isM3u8) "HLS" else "MP4",
                            fixedLink,
                            type = if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                        ) {
                            this.referer = mainUrl
                        }
                    )
                    found = true
                }
            }
        }

        // 2. Extract embedded iFrames & load them through standard Extractors
        document.select("iframe[src], embed[src]").mapNotNull { 
            fixUrlNull(it.attr("src")) 
        }.forEach { embedUrl ->
            if (loadExtractor(embedUrl, mainUrl, subtitleCallback, callback)) {
                found = true
            }
        }

        return found
    }
}