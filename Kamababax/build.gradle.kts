plugins {
    // Make sure the Cloudstream extension plugin is applied here
    id("com.lagradost.cloudstream3.gradle") // Use the template's current version
}

version = 1

cloudstream {
    authors = listOf("dodo")
    language = "en" // or multi
    description = "Kamababax - Desi Indian sex videos"
    status = 1 // Working
    tvTypes = listOf("NSFW")
    iconUrl = "https://www.kamababax.com/favicon.ico" // optional
}