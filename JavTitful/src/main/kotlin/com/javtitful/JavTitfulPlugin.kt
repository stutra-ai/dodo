import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.extractors.EmturbovidExtractor
import com.lagradost.cloudstream3.extractors.FileMoonSx
import com.lagradost.cloudstream3.extractors.StreamTape
import com.lagradost.cloudstream3.extractors.VidStack
import com.lagradost.cloudstream3.extractors.Voe
import com.lagradost.cloudstream3.plugins.BasePlugin

@CloudstreamPlugin
class JavTifulPlugin : BasePlugin() {
    override fun load() {
        // 1. Register Main Site Scraper
        registerMainAPI(JavTiful())

        // 2. Register Built-in CloudStream Extractors
        registerExtractorAPI(EmturbovidExtractor())
        registerExtractorAPI(VidStack())
        registerExtractorAPI(Voe())
        
        // 3. Register ALL Custom Extractors from Extractorlar.kt
        
        // VidHide Network
        registerExtractorAPI(VidHidePro())
        registerExtractorAPI(VidhideVIP())
        registerExtractorAPI(Javlion())
        registerExtractorAPI(VidHidePro1())
        registerExtractorAPI(VidHidePro2())
        registerExtractorAPI(VidHidePro3())
        registerExtractorAPI(VidHidePro4())
        registerExtractorAPI(VidHidePro6())
        registerExtractorAPI(VidHidePro7())
        registerExtractorAPI(Dhcplay())
        registerExtractorAPI(Smoothpre())
        registerExtractorAPI(Dhtpre())
        registerExtractorAPI(Peytonepre())
        registerExtractorAPI(Movearnpre())
        registerExtractorAPI(Dintezuvio())
        registerExtractorAPI(HgLink())
        registerExtractorAPI(RyderJet())
        registerExtractorAPI(MyCloudZ())

        // Lulu Network
        registerExtractorAPI(LULUSTREAM())
        registerExtractorAPI(LULUVDO())
        registerExtractorAPI(LULUVDOO())
        registerExtractorAPI(LULUPVP())
        registerExtractorAPI(LULUDLC())
        registerExtractorAPI(LULU0())
        registerExtractorAPI(LULUX08())

        // Player4Me Network
        registerExtractorAPI(Player4Me())
        registerExtractorAPI(Vip4me())
        registerExtractorAPI(RPMShare())
        registerExtractorAPI(UpnsOnline())
        registerExtractorAPI(EmbedSeek())
        registerExtractorAPI(VipSeekPlayer())
        registerExtractorAPI(EasyVidPlayer())
        registerExtractorAPI(VipEasyVidPlayer())

        // DoodStream Network
        registerExtractorAPI(DoodStream())
        registerExtractorAPI(Playmogo())
        registerExtractorAPI(DoodDoply())
        registerExtractorAPI(DoodPmExtractor())
        registerExtractorAPI(DoodVideo())
        registerExtractorAPI(Ds2Play())
        registerExtractorAPI(d000d())
        registerExtractorAPI(Dooood())

        // StreamTAPE Network
        registerExtractorAPI(StreamTAPE())
        registerExtractorAPI(Watchadsontape())
        registerExtractorAPI(Stape())
        registerExtractorAPI(StreamTapeNet())
        registerExtractorAPI(StreamTapeXyz())
        registerExtractorAPI(ShaveTape())

        // FileMoon Network
        registerExtractorAPI(Filemoon())
        registerExtractorAPI(FileMoon2())
        registerExtractorAPI(FileMoonIn())
        registerExtractorAPI(FileMoonSx())
        registerExtractorAPI(Bysedikamoum())
        registerExtractorAPI(Bysezoexe())
        registerExtractorAPI(Filemoonx08())

        // Streamwish / Misc
        registerExtractorAPI(Streamwish())
        registerExtractorAPI(Streamhihi())
        registerExtractorAPI(Javsw())
        registerExtractorAPI(Turtleviplay())
        registerExtractorAPI(Turboviplay())
        registerExtractorAPI(Vidguardto())
        registerExtractorAPI(CloudWish())
        registerExtractorAPI(Javclan())
        registerExtractorAPI(Javggvideo())
        registerExtractorAPI(swhoi())
        registerExtractorAPI(Javmoon())
        registerExtractorAPI(StbP2P())
        registerExtractorAPI(Playerupnone())
        registerExtractorAPI(MixDropis())
        registerExtractorAPI(MixDropAG())
        registerExtractorAPI(MixDropMy())
        registerExtractorAPI(VidNest())
        registerExtractorAPI(Maxstream())
        registerExtractorAPI(Lancewhoisdifficult())
        registerExtractorAPI(Javlesbians())
    }
}