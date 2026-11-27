package com.bareminimumstudios.temperandtang.platform
import com.bareminimumstudios.temperandtang.StationVerification
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.TemperCommands
import com.bareminimumstudios.temperandtang.client.TemperClient
import com.bareminimumstudios.temperandtang.client.ThermalTooltips
import com.bareminimumstudios.temperandtang.compat.TemplateLoot
import com.bareminimumstudios.temperandtang.content.OreGenerationFilter
import com.bareminimumstudios.temperandtang.content.TemperContent
import com.bareminimumstudios.temperandtang.content.WorkshopCatalog
import com.bareminimumstudios.temperandtang.smithing.AlloyRecipes
import com.bareminimumstudios.temperandtang.smithing.HotMetalHandling
import com.bareminimumstudios.temperandtang.smithing.PrecisionRefining
import com.bareminimumstudios.temperandtang.smithing.SteelUpgrades
import com.bareminimumstudios.temperandtang.smithing.ThermalRecipes
import com.bareminimumstudios.temperandtang.smithing.WorkshopFuel
import com.bareminimumstudios.temperandtang.station.WorkshopRecovery
import java.util.function.Supplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.block.Block
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.extensions.IItemStackExtension
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.LootTableLoadEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.registries.DeferredRegister

@Mod(TemperAndTang.MOD_ID)
class TemperNeoForge(bus: IEventBus) {
    private val armorMaterials = DeferredRegister.create(Registries.ARMOR_MATERIAL,TemperAndTang.MOD_ID)
    private val blocks = DeferredRegister.create(Registries.BLOCK, TemperAndTang.MOD_ID)
    private val items = DeferredRegister.create(Registries.ITEM, TemperAndTang.MOD_ID)
    private val recipeTypes = DeferredRegister.create(Registries.RECIPE_TYPE, TemperAndTang.MOD_ID)
    private val recipeSerializers = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TemperAndTang.MOD_ID)
    init {
        val placementModifiers = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, TemperAndTang.MOD_ID)
        placementModifiers.register("tin_enabled", Supplier { OreGenerationFilter.TIN })
        placementModifiers.register("silver_enabled", Supplier { OreGenerationFilter.SILVER })
        placementModifiers.register(bus)
        WorkshopFuel.burnTime={ (it as IItemStackExtension).getBurnTime(RecipeType.SMELTING) }
        WorkshopFuel.remainder={ (it as IItemStackExtension).craftingRemainingItem }

        WorkshopCatalog.armorMaterials.forEach { (name,factory) -> armorMaterials.register(name,Supplier {factory()}) }
        armorMaterials.register(bus)
        recipeTypes.register("shaping", Supplier { ThermalRecipes.TYPE })
        recipeSerializers.register("shaping", Supplier { ThermalRecipes.SERIALIZER })
        recipeSerializers.register("shaping_shapeless", Supplier { ThermalRecipes.SHAPELESS })
        recipeTypes.register("alloying",Supplier {AlloyRecipes.TYPE})
        recipeSerializers.register("alloying",Supplier {AlloyRecipes.SERIALIZER})
        recipeSerializers.register("precision_refining",Supplier {PrecisionRefining.SERIALIZER})
        recipeSerializers.register("steel_upgrade",Supplier {SteelUpgrades.SERIALIZER})
        recipeTypes.register(bus)
        recipeSerializers.register(bus)
        TemperContent.blocks.forEach { (name, factory) ->
            val block = blocks.register(name, Supplier<Block> { factory() })
            items.register(name, Supplier<Item> { BlockItem(block.get(), Item.Properties()) })
        }
        TemperContent.items.forEach { (name, factory) -> items.register(name, Supplier<Item> { factory() }) }
        NeoForge.EVENT_BUS.addListener { event: LootTableLoadEvent -> TemplateLoot.pool(event.name)?.let {event.table.addPool(it.build())} }
        NeoForge.EVENT_BUS.addListener { event: RegisterCommandsEvent -> TemperCommands.register(event.dispatcher) }
        NeoForge.EVENT_BUS.addListener { event: ServerTickEvent.Post -> HotMetalHandling.tick(event.server); StationVerification.tick(event.server) }
        NeoForge.EVENT_BUS.addListener { event: ServerStoppingEvent -> WorkshopRecovery.stopping(event.server) }
        NeoForge.EVENT_BUS.addListener { event: ServerStoppedEvent -> StationVerification.stop(event.server); WorkshopRecovery.clear(event.server); HotMetalHandling.clear() }
        bus.addListener { event: FMLClientSetupEvent -> event.enqueueWork { TemperClient.initialize(); NeoForge.EVENT_BUS.addListener { tooltip: ItemTooltipEvent -> ThermalTooltips.append(tooltip.itemStack,tooltip.toolTip) } } }
        blocks.register(bus)
        items.register(bus)
        bus.addListener { event: FMLCommonSetupEvent -> event.enqueueWork { TemperAndTang.initialize { ModList.get().isLoaded(it) } } }
        bus.addListener { event: BuildCreativeModeTabContentsEvent ->
            (TemperContent.blocks.keys+TemperContent.items.keys).filter {it!="unrefined_netherite_alloy"}.map(TemperContent::item).filter {WorkshopCatalog.tab(it)==event.tabKey}.forEach {event.accept(it)}
        }
    }
}
