package com.javtitful

import android.util.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.extractors.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.USER_AGENT
import com.lagradost.cloudstream3.utils.M3u8Helper.Companion.generateM3u8
import org.jsoup.Jsoup
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.mapper

// Copy the extractor classes from the reference code here
// You can include the same extractors or only the ones you need

// Basic extractor example for a simple video site
open class SimpleExtractor : ExtractorApi() {
    override var name = "SimpleExtractor"
    override var mainUrl = "https://javtitful.com"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            val response = app.get(url, referer = referer)
            val html = response.text

            // Try to find video sources
            val m3u8Regex = Regex("""(https?://[^"'\s]+\.m3u8[^"'\s]*)""")
            val m3u8Matches = m3u8Regex.findAll(html).toList()
            
            if (m3u8Matches.isNotEmpty()) {
                for (match in m3u8Matches) {
                    val videoUrl = match.groupValues[1]
                    callback.invoke(
                        newExtractorLink(
                            source = name,
                            name = "Video",
                            url = videoUrl,
                            type = ExtractorLinkType.M3U8
                        ) {
                            this.referer = mainUrl
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
            } else {
                // Try to find mp4 links
                val mp4Regex = Regex("""(https?://[^"'\s]+\.mp4[^"'\s]*)""")
                val mp4Matches = mp4Regex.findAll(html).toList()
                
                for (match in mp4Matches) {
                    val videoUrl = match.groupValues[1]
                    callback.invoke(
                        newExtractorLink(
                            source = name,
                            name = "Video",
                            url = videoUrl,
                            type = ExtractorLinkType.VIDEO
                        ) {
                            this.referer = mainUrl
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Log.d("SimpleExtractor", "Error: ${e.message}")
        }
    }
}