package com.bareminimumstudios.temperandtang
import com.bareminimumstudios.temperandtang.station.FinishingStation
import me.fzzyhmstrs.fzzy_config.api.ConfigApi
import net.minecraft.resources.ResourceLocation
import org.slf4j.LoggerFactory

object TemperAndTang {
    const val MOD_ID = "temperandtang"
    val logger = LoggerFactory.getLogger(MOD_ID)
    var config = TemperConfig()
        private set
    var modLoaded: (String) -> Boolean = { false }
    fun id(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    fun initialize(presence: (String) -> Boolean) {
        modLoaded = presence
        config = ConfigApi.registerAndLoadConfig(::TemperConfig)
        FinishingStation.register()
        logger.info("Temper & Tang ready: Diegesis finishing station; Simply Swords={}, Overgeared={}",
            presence("simplyswords"), presence("overgeared"))
    }
}
