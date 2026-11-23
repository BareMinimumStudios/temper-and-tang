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
    private val blocks = DeferredRegister.create(Registries.BLOCK, TemperAndTang.MOD_ID)
    private val items = DeferredRegister.create(Registries.ITEM, TemperAndTang.MOD_ID)
    private val recipeTypes = DeferredRegister.create(Registries.RECIPE_TYPE, TemperAndTang.MOD_ID)
    private val recipeSerializers = DeferredRegister.create(Registries.RECIPE_SERIALIZER, TemperAndTang.MOD_ID)
    init {
        recipeTypes.register("shaping", java.util.function.Supplier { com.bareminimumstudios.temperandtang.smithing.ThermalRecipes.TYPE })
        recipeSerializers.register("shaping", java.util.function.Supplier { com.bareminimumstudios.temperandtang.smithing.ThermalRecipes.SERIALIZER })
        recipeTypes.register(bus)
        recipeSerializers.register(bus)
        TemperContent.blocks.forEach { (name, factory) ->
            val block = blocks.register(name, java.util.function.Supplier<Block> { factory() })
            items.register(name, java.util.function.Supplier<Item> { BlockItem(block.get(), Item.Properties()) })
        }
        TemperContent.items.forEach { (name, factory) -> items.register(name, java.util.function.Supplier<Item> { factory() }) }
        NeoForge.EVENT_BUS.addListener { event: RegisterCommandsEvent -> TemperCommands.register(event.dispatcher) }
        NeoForge.EVENT_BUS.addListener { event: ServerTickEvent.Post -> StationVerification.tick(event.server) }
        NeoForge.EVENT_BUS.addListener { event: ServerStoppedEvent -> StationVerification.stop(event.server) }
        bus.addListener { event: FMLClientSetupEvent -> event.enqueueWork { com.bareminimumstudios.temperandtang.client.TemperClient.initialize() } }
        blocks.register(bus)
        items.register(bus)
        bus.addListener { event: FMLCommonSetupEvent -> event.enqueueWork { TemperAndTang.initialize { ModList.get().isLoaded(it) } } }
        bus.addListener { event: BuildCreativeModeTabContentsEvent ->
            if (event.tabKey == CreativeModeTabs.INGREDIENTS)
                (TemperContent.blocks.keys + TemperContent.items.keys).forEach { event.accept(TemperContent.item(it)) }
        }
    }
}
