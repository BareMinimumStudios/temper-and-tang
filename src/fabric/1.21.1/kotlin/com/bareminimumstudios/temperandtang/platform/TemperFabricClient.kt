package com.bareminimumstudios.temperandtang.platform
import com.bareminimumstudios.temperandtang.client.TemperClient
import com.bareminimumstudios.temperandtang.client.ThermalTooltips
import com.bareminimumstudios.temperandtang.content.TemperContent
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.renderer.RenderType

object TemperFabricClient : ClientModInitializer {
    override fun onInitializeClient() {
        TemperClient.initialize()
        val cutout = RenderType.cutout()
        for (name in listOf("charcoal_hearth", "alloy_forge", "nether_alloy_forge")) {
            BlockRenderLayerMap.INSTANCE.putBlock(TemperContent.block(name), cutout)
        }
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            ThermalTooltips.append(stack, lines)
        }
    }
}
