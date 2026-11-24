package com.bareminimumstudios.temperandtang.platform
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.TemperCommands
import com.bareminimumstudios.temperandtang.StationVerification
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import com.bareminimumstudios.temperandtang.content.TemperContent
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.registries.DeferredRegister

@Mod(TemperAndTang.MOD_ID)
class TemperNeoForge(bus: IEventBus) {
    private val armorMaterials = DeferredRegister.create(Registries.ARMOR_MATERIAL,TemperAndTang.MOD_ID)
    private val blocks = DeferredRegister.create(Registries.BLOCK, TemperAndTang.MOD_ID)
    private val items = DeferredRegister.create(Registries.ITEM, TemperAndTang.MOD_ID)
    private val recipeTypes = DeferredRegister.create(Registries.RECIPE_TYPE, TemperAndTang.MOD_ID)
    private val recipeSerializers = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TemperAndTang.MOD_ID)
    init {
        com.bareminimumstudios.temperandtang.smithing.WorkshopFuel.burnTime={ (it as net.neoforged.neoforge.common.extensions.IItemStackExtension).getBurnTime(net.minecraft.world.item.crafting.RecipeType.SMELTING) }
        com.bareminimumstudios.temperandtang.smithing.WorkshopFuel.remainder={ (it as net.neoforged.neoforge.common.extensions.IItemStackExtension).craftingRemainingItem }

        com.bareminimumstudios.temperandtang.content.WorkshopCatalog.armorMaterials.forEach { (name,factory) -> armorMaterials.register(name,java.util.function.Supplier {factory()}) }
        armorMaterials.register(bus)
        recipeTypes.register("shaping", java.util.function.Supplier { com.bareminimumstudios.temperandtang.smithing.ThermalRecipes.TYPE })
        recipeSerializers.register("shaping", java.util.function.Supplier { com.bareminimumstudios.temperandtang.smithing.ThermalRecipes.SERIALIZER })
        recipeSerializers.register("shaping_shapeless", java.util.function.Supplier { com.bareminimumstudios.temperandtang.smithing.ThermalRecipes.SHAPELESS })
        recipeTypes.register("alloying",java.util.function.Supplier {com.bareminimumstudios.temperandtang.smithing.AlloyRecipes.TYPE})
        recipeSerializers.register("alloying",java.util.function.Supplier {com.bareminimumstudios.temperandtang.smithing.AlloyRecipes.SERIALIZER})
        recipeSerializers.register("precision_refining",java.util.function.Supplier {com.bareminimumstudios.temperandtang.smithing.PrecisionRefining.SERIALIZER})
        recipeSerializers.register("steel_upgrade",java.util.function.Supplier {com.bareminimumstudios.temperandtang.smithing.SteelUpgrades.SERIALIZER})
        recipeTypes.register(bus)
        recipeSerializers.register(bus)
        TemperContent.blocks.forEach { (name, factory) ->
            val block = blocks.register(name, java.util.function.Supplier<Block> { factory() })
            items.register(name, java.util.function.Supplier<Item> { BlockItem(block.get(), Item.Properties()) })
        }
        TemperContent.items.forEach { (name, factory) -> items.register(name, java.util.function.Supplier<Item> { factory() }) }
        NeoForge.EVENT_BUS.addListener { event: net.neoforged.neoforge.event.LootTableLoadEvent -> com.bareminimumstudios.temperandtang.compat.TemplateLoot.pool(event.name)?.let {event.table.addPool(it.build())} }
        NeoForge.EVENT_BUS.addListener { event: RegisterCommandsEvent -> TemperCommands.register(event.dispatcher) }
        NeoForge.EVENT_BUS.addListener { event: ServerTickEvent.Post -> com.bareminimumstudios.temperandtang.smithing.HotMetalHandling.tick(event.server); StationVerification.tick(event.server) }
        NeoForge.EVENT_BUS.addListener { event: net.neoforged.neoforge.event.server.ServerStoppingEvent -> com.bareminimumstudios.temperandtang.station.WorkshopRecovery.stopping(event.server) }
        NeoForge.EVENT_BUS.addListener { event: ServerStoppedEvent -> StationVerification.stop(event.server); com.bareminimumstudios.temperandtang.station.WorkshopRecovery.clear(event.server); com.bareminimumstudios.temperandtang.smithing.HotMetalHandling.clear() }
        bus.addListener { event: FMLClientSetupEvent -> event.enqueueWork { com.bareminimumstudios.temperandtang.client.TemperClient.initialize(); NeoForge.EVENT_BUS.addListener { tooltip: net.neoforged.neoforge.event.entity.player.ItemTooltipEvent -> com.bareminimumstudios.temperandtang.client.ThermalTooltips.append(tooltip.itemStack,tooltip.toolTip) } } }
        blocks.register(bus)
        items.register(bus)
        bus.addListener { event: FMLCommonSetupEvent -> event.enqueueWork { TemperAndTang.initialize { ModList.get().isLoaded(it) } } }
        bus.addListener { event: BuildCreativeModeTabContentsEvent ->
            (TemperContent.blocks.keys+TemperContent.items.keys).filter {it!="unrefined_netherite_alloy"}.map(TemperContent::item).filter {com.bareminimumstudios.temperandtang.content.WorkshopCatalog.tab(it)==event.tabKey}.forEach {event.accept(it)}
        }
    }
}
