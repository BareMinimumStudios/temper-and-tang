package com.bareminimumstudios.temperandtang.content
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.smithing.Finish
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.*
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.state.BlockBehaviour

object TemperContent {
    val blocks: Map<String, () -> Block> = linkedMapOf(
        "tin_ore" to { Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)) },
        "deepslate_tin_ore" to { Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)) },
        "tin_block" to { Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)) },
        "bronze_block" to { Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)) },
        "steel_block" to { Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)) },
        "raw_tin_block" to { Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)) },
        "charcoal_hearth" to { WorkshopBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE).lightLevel { 0 }.noOcclusion(),"hearth") },
        "shaping_anvil" to { WorkshopBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion(),"forge") },
        "quenching_trough" to { WorkshopBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion(),"trough") },
        "finishing_anvil" to { WorkshopBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion(),"finishing") },
    )
    val items: Map<String, () -> Item> = linkedMapOf(
        "unquenched_weapon" to { Item(Item.Properties().stacksTo(1)) },
        "raw_tin" to { Item(Item.Properties()) },
        "tin_ingot" to { Item(Item.Properties()) },
        "tin_nugget" to { Item(Item.Properties()) },
        "steel_ingot" to { Item(Item.Properties()) },
        "steel_nugget" to { Item(Item.Properties()) },
        "bronze_ingot" to { Item(Item.Properties()) },
        "bronze_blend" to { Item(Item.Properties()) },
        "bronze_nugget" to { Item(Item.Properties()) },
        "bronze_sword" to { SwordItem(BronzeTier, Item.Properties().attributes(SwordItem.createAttributes(BronzeTier, 3, -2.4f))) },
        "smithing_flux" to { Item(Item.Properties()) },
        "blank_blueprint" to { Item(Item.Properties()) },
        "smithing_blueprint" to { Item(Item.Properties().stacksTo(1)) },
        "smithing_tongs" to { SmithingTongsItem(Item.Properties().durability(256).stacksTo(1)) },
        "smithing_hammer" to { SmithingHammerItem(Item.Properties().durability(384).stacksTo(1)) },
        "tempering_compound" to { FinishingMaterialItem(Finish.TEMPERED, Item.Properties()) },
        "honing_compound" to { FinishingMaterialItem(Finish.KEEN, Item.Properties()) },
        "balance_weight" to { FinishingMaterialItem(Finish.WEIGHTED, Item.Properties()) },
        "lightweight_grip" to { FinishingMaterialItem(Finish.LIGHTENED, Item.Properties()) },
    ) + WorkshopCatalog.items
    fun block(name: String): Block = BuiltInRegistries.BLOCK.get(TemperAndTang.id(name))
    fun item(name: String): Item = BuiltInRegistries.ITEM.get(TemperAndTang.id(name))
}
