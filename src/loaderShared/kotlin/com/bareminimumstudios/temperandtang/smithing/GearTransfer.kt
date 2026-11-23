package com.bareminimumstudios.temperandtang.smithing
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/** Upgrade patterns retain names, enchantments, wear and foreign data without copying the old material's base stats. */
object GearTransfer {
    fun inherit(result:ItemStack,base:ItemStack?):ItemStack {
        if(base==null || result.isEmpty)return result
        base.get(DataComponents.CUSTOM_NAME)?.let {result.set(DataComponents.CUSTOM_NAME,it)}
        base.get(DataComponents.ENCHANTMENTS)?.let {result.set(DataComponents.ENCHANTMENTS,it)}
        base.get(DataComponents.REPAIR_COST)?.let {result.set(DataComponents.REPAIR_COST,it)}
        val foreign=base.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();foreign.remove("temperandtang")
        val own=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();own.merge(foreign)
        if(!own.isEmpty)result.set(DataComponents.CUSTOM_DATA,CustomData.of(own))
        result.damageValue=base.damageValue
        return result
    }
}
