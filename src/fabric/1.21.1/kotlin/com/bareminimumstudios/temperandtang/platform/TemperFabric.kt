package com.bareminimumstudios.temperandtang.platform
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.TemperCommands
import com.bareminimumstudios.temperandtang.StationVerification
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import com.bareminimumstudios.temperandtang.content.TemperContent
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.levelgen.GenerationStep

object TemperFabric : ModInitializer {
    override fun onInitialize() {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, TemperAndTang.id("shaping"), com.bareminimumstudios.temperandtang.smithing.ThermalRecipes.TYPE)
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TemperAndTang.id("shaping"), com.bareminimumstudios.temperandtang.smithing.ThermalRecipes.SERIALIZER)
        TemperContent.blocks.forEach { (name, factory) ->
            val block = Registry.register(BuiltInRegistries.BLOCK, TemperAndTang.id(name), factory())
            Registry.register(BuiltInRegistries.ITEM, TemperAndTang.id(name), BlockItem(block, Item.Properties()))
        }
        TemperContent.items.forEach { (name, factory) -> Registry.register(BuiltInRegistries.ITEM, TemperAndTang.id(name), factory()) }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register { entries ->
            (TemperContent.blocks.keys + TemperContent.items.keys).forEach { entries.accept(TemperContent.item(it)) }
        }
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
            ResourceKey.create(Registries.PLACED_FEATURE, TemperAndTang.id("tin_ore")))
        TemperAndTang.initialize { FabricLoader.getInstance().isModLoaded(it) }
        ServerTickEvents.END_SERVER_TICK.register(StationVerification::tick)
        ServerLifecycleEvents.SERVER_STOPPED.register(StationVerification::stop)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> TemperCommands.register(dispatcher) }
    }
}
