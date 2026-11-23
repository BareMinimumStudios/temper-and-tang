package com.bareminimumstudios.temperandtang.platform
import com.bareminimumstudios.temperandtang.client.TemperClient
import net.fabricmc.api.ClientModInitializer
object TemperFabricClient : ClientModInitializer {
    override fun onInitializeClient() { TemperClient.initialize(); net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register { stack, _, _, lines -> com.bareminimumstudios.temperandtang.client.ThermalTooltips.append(stack,lines) } }
}
