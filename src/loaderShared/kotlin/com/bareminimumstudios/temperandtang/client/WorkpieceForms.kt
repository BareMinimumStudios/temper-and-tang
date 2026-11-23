package com.bareminimumstudios.temperandtang.client

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.*

/** Shapes are visual only: the authored recipe and equipment item remain unchanged. */
object WorkpieceForms {
    fun model(item:Item):String {
        if(item is ArmorItem)return when(item.equipmentSlot) {
            EquipmentSlot.HEAD->"helmet_blank";EquipmentSlot.CHEST->"chestplate_blank";EquipmentSlot.LEGS->"leggings_blank";else->"boots_blank"
        }
        if(item is ShieldItem)return "shield_blank"
        if(item is PickaxeItem)return "pickaxe_blank"
        if(item is AxeItem)return "axe_blank"
        if(item is ShovelItem)return "shovel_blank"
        if(item is HoeItem)return "hoe_blank"
        val path=BuiltInRegistries.ITEM.getKey(item).path
        return when {
            "pickaxe" in path->"pickaxe_blank"
            "scythe" in path->"scythe_blank"
            "axe" in path || "halberd" in path->"axe_blank"
            listOf("spear","pike","glaive","lance","javelin","trident").any {it in path}->"spear_blank"
            "hammer" in path || "mace" in path->"hammer_blank"
            else->"blade_blank"
        }
    }
    fun plate(item:Item)=item is ArmorItem || item is ShieldItem
}
