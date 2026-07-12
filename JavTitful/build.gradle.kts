plugins {
    // Make sure the Cloudstream extension plugin is applied here
    id("com.lagradost.cloudstream3.gradle") // Use the template's current version
}

version = 1

cloudstream {
    authors = listOf("dodo")
    language = "en" // or multi
    description = "Javtitful"
    status = 1 // Working
    tvTypes = listOf("NSFW")
    iconUrl = "https://cdn.pixabay.com/photo/2024/10/22/12/41/q-9139532_960_720.png" // optional
}