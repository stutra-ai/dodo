package com.kamababax

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.BasePlugin

@CloudstreamPlugin
class KamababaxPlugin: BasePlugin() {
    override fun load() {
        // Correct initialization using parentheses: Kamababax()
        registerMainAPI(Kamababax())
    }
}