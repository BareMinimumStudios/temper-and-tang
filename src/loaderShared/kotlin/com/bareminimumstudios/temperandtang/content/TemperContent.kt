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
        "raw_tin_block" to { Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)) },
        "finishing_anvil" to { FinishingAnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL)) },
    )
    val items: Map<String, () -> Item> = linkedMapOf(
        "raw_tin" to { Item(Item.Properties()) },
        "tin_ingot" to { Item(Item.Properties()) },
        "tin_nugget" to { Item(Item.Properties()) },
        "bronze_ingot" to { Item(Item.Properties()) },
        "bronze_blend" to { Item(Item.Properties()) },
        "bronze_nugget" to { Item(Item.Properties()) },
        "bronze_sword" to { SwordItem(BronzeTier, Item.Properties().attributes(SwordItem.createAttributes(BronzeTier, 3, -2.4f))) },
        "smithing_hammer" to { SmithingHammerItem(Item.Properties().durability(384).stacksTo(1)) },
        "tempering_compound" to { FinishingMaterialItem(Finish.TEMPERED, Item.Properties()) },
        "honing_compound" to { FinishingMaterialItem(Finish.KEEN, Item.Properties()) },
        "balance_weight" to { FinishingMaterialItem(Finish.WEIGHTED, Item.Properties()) },
        "lightweight_grip" to { FinishingMaterialItem(Finish.LIGHTENED, Item.Properties()) },
    )
    fun block(name: String): Block = BuiltInRegistries.BLOCK.get(TemperAndTang.id(name))
    fun item(name: String): Item = BuiltInRegistries.ITEM.get(TemperAndTang.id(name))
}
