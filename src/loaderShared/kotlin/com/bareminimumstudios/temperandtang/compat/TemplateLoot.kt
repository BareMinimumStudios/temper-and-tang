package com.bareminimumstudios.temperandtang.compat
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.content.TemperContent
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue

object TemplateLoot {
    val rare=setOf("stronghold_corridor","stronghold_crossing","stronghold_library","desert_pyramid","shipwreck_treasure","woodland_mansion","jungle_temple","ancient_city","pillager_outpost","buried_treasure")
    val common=setOf("abandoned_mineshaft","simple_dungeon")
    fun pool(id:ResourceLocation):LootPool.Builder? {
        if(!TemperAndTang.config.diamondUpgrades.get() || id.namespace!="minecraft" || !id.path.startsWith("chests/"))return null
        val chance=when(id.path.removePrefix("chests/")){in rare->.5f;in common->.15f;else->return null}
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1f)).`when`(LootItemRandomChanceCondition.randomChance(chance))
            .add(LootItem.lootTableItem(TemperContent.item("diamond_upgrade_smithing_template")))
    }
}
