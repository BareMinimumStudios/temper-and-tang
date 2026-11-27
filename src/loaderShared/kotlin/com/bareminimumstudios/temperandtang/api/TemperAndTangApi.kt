package com.bareminimumstudios.temperandtang.api
import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.smithing.GearSupport
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Predicate
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

/** Register during common setup on both sides. Datapack tags are preferred for static items. */
object TemperAndTangApi {
    @JvmField val FINISHABLE_WEAPONS: TagKey<Item> = TagKey.create(Registries.ITEM, TemperAndTang.id("finishable_weapons"))
    @JvmField val EXCLUDED_WEAPONS: TagKey<Item> = TagKey.create(Registries.ITEM, TemperAndTang.id("excluded_weapons"))
    @JvmField val ALLOY_ADDITIVES: TagKey<Item> = TagKey.create(Registries.ITEM, TemperAndTang.id("alloy_additives"))
    @JvmField val TONGS: TagKey<Item> = TagKey.create(Registries.ITEM, TemperAndTang.id("tongs"))
    @JvmField val FORGEABLE_EQUIPMENT=GearSupport.INCLUDED
    @JvmField val EXCLUDED_EQUIPMENT=GearSupport.EXCLUDED
    @JvmStatic fun registerEquipmentPredicate(predicate:Predicate<ItemStack>) {registerWeaponPredicate(predicate)}
    private val predicates = CopyOnWriteArrayList<Predicate<ItemStack>>()
    @JvmStatic fun registerWeaponPredicate(predicate: Predicate<ItemStack>) { predicates.add(predicate) }
    fun acceptsCustom(stack: ItemStack): Boolean = predicates.any { it.test(stack) }
    @JvmStatic fun finishingMaterialTag(finish: String): TagKey<Item> {
        require(finish in setOf("tempered", "keen", "weighted", "lightened")) { "Unknown built-in finish: $finish" }
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("temperandtang", "finishing_materials/$finish"))
    }
}
