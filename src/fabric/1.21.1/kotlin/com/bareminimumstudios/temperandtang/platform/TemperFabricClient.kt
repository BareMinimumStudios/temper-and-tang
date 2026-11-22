package com.bareminimumstudios.temperandtang.platform
import com.bareminimumstudios.temperandtang.client.TemperClient
import net.fabricmc.api.ClientModInitializer
object TemperFabricClient : ClientModInitializer {
    override fun onInitializeClient() { TemperClient.initialize() }
}
