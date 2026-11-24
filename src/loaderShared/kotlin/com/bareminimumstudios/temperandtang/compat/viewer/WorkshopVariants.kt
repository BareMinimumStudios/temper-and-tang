package com.bareminimumstudios.temperandtang.compat.viewer
import com.bareminimumstudios.temperandtang.smithing.*
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

/** Only recipe-relevant differences affect viewer identity; quality, names and wear do not. */
object WorkshopVariants {
    fun ironFamily(stack:ItemStack)=GearSupport.steelCore(stack)
    fun key(stack:ItemStack)=if(Blueprints.filled(stack))Blueprints.pattern(stack)else if(ironFamily(stack) && CraftMaterials.recorded(stack)==ForgeMaterial.STEEL)"steel_core"else "normal"
    fun steel(stack:ItemStack):ItemStack {
        val result=CraftMaterials.stamp(stack,ForgeMaterial.STEEL)
        val all=result.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();val root=all.getCompound("temperandtang")
        root.putInt("origin_durability",stack.maxDamage);all.put("temperandtang",root);result.set(DataComponents.CUSTOM_DATA,CustomData.of(all))
        return GearStats.rebuild(result)
    }
    fun examples()=BuiltInRegistries.ITEM.map {ItemStack(it)}.filter(::ironFamily).map(::steel)
    fun alternatives(stack:ItemStack)=if(ironFamily(stack) && key(stack)=="normal")listOf(stack,steel(stack))else listOf(stack)
}
