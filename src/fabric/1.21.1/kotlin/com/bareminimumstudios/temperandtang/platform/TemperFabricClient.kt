package com.bareminimumstudios.temperandtang.platform
import com.bareminimumstudios.temperandtang.client.TemperClient
import net.fabricmc.api.ClientModInitializer
object TemperFabricClient : ClientModInitializer {
    override fun onInitializeClient() { TemperClient.initialize();
        for(name in listOf("charcoal_hearth","alloy_furnace","nether_alloy_furnace"))net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap.INSTANCE.putBlock(com.bareminimumstudios.temperandtang.content.TemperContent.block(name),net.minecraft.client.renderer.RenderType.cutout())
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.BLOCK.register({_,_,_,_->0xB39DA8},com.bareminimumstudios.temperandtang.content.TemperContent.block("nether_alloy_furnace"))
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register({_,_->0xB39DA8},com.bareminimumstudios.temperandtang.content.TemperContent.item("nether_alloy_furnace"))
         net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register { stack, _, _, lines -> com.bareminimumstudios.temperandtang.client.ThermalTooltips.append(stack,lines) } }
}
