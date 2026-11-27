package com.bareminimumstudios.temperandtang.platform
import com.bareminimumstudios.temperandtang.StationVerification
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.TemperCommands
import com.bareminimumstudios.temperandtang.compat.TemplateLoot
import com.bareminimumstudios.temperandtang.content.OreGenerationFilter
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.content.WorkshopCatalog
import com.bareminimumstudios.temperandtang.smithing.AlloyRecipes
import com.bareminimumstudios.temperandtang.smithing.HotMetalHandling
import com.bareminimumstudios.temperandtang.smithing.PrecisionRefining
import com.bareminimumstudios.temperandtang.smithing.SteelUpgrades
import com.bareminimumstudios.temperandtang.smithing.ThermalRecipes
import com.bareminimumstudios.temperandtang.station.WorkshopRecovery
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.fabricmc.loader.api.FabricLoader
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
        Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, TemperAndTang.id("tin_enabled"), OreGenerationFilter.TIN)
        Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, TemperAndTang.id("silver_enabled"), OreGenerationFilter.SILVER)
        Registry.register(BuiltInRegistries.RECIPE_TYPE, TemperAndTang.id("shaping"), ThermalRecipes.TYPE)
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TemperAndTang.id("shaping_shapeless"), ThermalRecipes.SHAPELESS)
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, TemperAndTang.id("shaping"), ThermalRecipes.SERIALIZER)
        WorkshopCatalog.armorMaterials.forEach { (name,factory) -> Registry.register(BuiltInRegistries.ARMOR_MATERIAL,TemperAndTang.id(name),factory()) }
        Registry.register(BuiltInRegistries.RECIPE_TYPE,TemperAndTang.id("alloying"),AlloyRecipes.TYPE)
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,TemperAndTang.id("alloying"),AlloyRecipes.SERIALIZER)
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,TemperAndTang.id("precision_refining"),PrecisionRefining.SERIALIZER)
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,TemperAndTang.id("steel_upgrade"),SteelUpgrades.SERIALIZER)
        TemperContent.blocks.forEach { (name, factory) ->
            val block = Registry.register(BuiltInRegistries.BLOCK, TemperAndTang.id(name), factory())
            Registry.register(BuiltInRegistries.ITEM, TemperAndTang.id(name), BlockItem(block, Item.Properties()))
        }
        TemperContent.items.forEach { (name, factory) -> Registry.register(BuiltInRegistries.ITEM, TemperAndTang.id(name), factory()) }
        for(tab in listOf(CreativeModeTabs.INGREDIENTS,CreativeModeTabs.TOOLS_AND_UTILITIES,CreativeModeTabs.COMBAT,CreativeModeTabs.FUNCTIONAL_BLOCKS,CreativeModeTabs.BUILDING_BLOCKS))
            ItemGroupEvents.modifyEntriesEvent(tab).register {entries ->
                (TemperContent.blocks.keys+TemperContent.items.keys).filter {it!="unrefined_netherite_alloy"}.map(TemperContent::item).filter {WorkshopCatalog.tab(it)==tab}.forEach {entries.accept(it)}
            }
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
            ResourceKey.create(Registries.PLACED_FEATURE, TemperAndTang.id("tin_ore")))
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES,
            ResourceKey.create(Registries.PLACED_FEATURE, TemperAndTang.id("silver_ore")))
        TemperAndTang.initialize { FabricLoader.getInstance().isModLoaded(it) }
        LootTableEvents.MODIFY.register {key,builder,_,_->TemplateLoot.pool(key.location())?.let {builder.withPool(it)} }
        ServerTickEvents.END_SERVER_TICK.register { server -> HotMetalHandling.tick(server); StationVerification.tick(server) }
        ServerLifecycleEvents.SERVER_STOPPING.register { server -> WorkshopRecovery.stopping(server) }
        ServerLifecycleEvents.SERVER_STOPPED.register { server -> StationVerification.stop(server); WorkshopRecovery.clear(server); HotMetalHandling.clear() }
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ -> TemperCommands.register(dispatcher) }
    }
}
