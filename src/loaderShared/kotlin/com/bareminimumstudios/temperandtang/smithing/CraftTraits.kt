package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.*
import net.minecraft.world.item.component.CustomData

object CraftTraits {
    fun recorded(stack:ItemStack)=ForgeTrait.parse(Craftsmanship.root(stack).getString("trait"))
    fun power(stack:ItemStack)=Craftsmanship.root(stack).getDouble("trait_power").takeIf {it.isFinite()}?.coerceIn(0.0,1.0)?:0.0
    fun family(stack:ItemStack)=when {GearSupport.armor(stack)->ForgeFamily.ARMOR;stack.item is ShieldItem->ForgeFamily.SHIELD;stack.item is DiggerItem->ForgeFamily.TOOL;else->ForgeFamily.WEAPON}
    fun stamp(input:ItemStack,trait:ForgeTrait):ItemStack {
        val result=input.copy();val all=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();val root=all.getCompound("temperandtang")
        root.putString("trait",trait.key);root.putDouble("trait_power",TemperAndTang.config.traitPower.get());root.putInt("trait_version",1)
        all.put("temperandtang",root);result.set(DataComponents.CUSTOM_DATA,CustomData.of(all));return result
    }
}
