package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.content.TemperContent
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.level.Level

object ThermalState {
    fun data(stack: ItemStack): CompoundTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("temperandtang").getCompound("thermal")
    private fun update(stack: ItemStack, edit: (CompoundTag) -> Unit) {
        val all = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
        val root = all.getCompound("temperandtang")
        val thermal = root.getCompound("thermal")
        edit(thermal)
        root.put("thermal", thermal)
        all.put("temperandtang", root)
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(all))
    }
    fun blank(stack: ItemStack) = !stack.isEmpty && stack.item == TemperContent.item("unquenched_weapon")
    fun pending(stack: ItemStack): Boolean = blank(stack) || data(stack).getLong("hot_until") > 0
    fun hot(stack: ItemStack, level: Level): Boolean {
        if (stack.isEmpty) return false
        val until = data(stack).getLong("hot_until")
        if (until > 0) return level.gameTime < until
        return false
    }
    fun heated(stack:ItemStack,level:Level):ItemStack=stack.copy().also {result->
        update(result) {it.putInt("version",1);it.putLong("hot_until",level.gameTime+TemperAndTang.config.workingHeatTicks.get());it.putString("stage","hot")}
    }
    fun normalize(stack:ItemStack,level:Level):ItemStack=if(data(stack).getLong("hot_until")>0 && !hot(stack,level))airCooled(stack,level) else stack
    fun airCooled(stack: ItemStack, level: Level): ItemStack {
        val result = stack.copy()
        update(result) { it.remove("hot_until"); it.putString("stage", if (blank(result)) "unquenched" else "cold") }
        return result
    }
    fun unquenched(weapon: ItemStack, level: Level): ItemStack {
        val result = ItemStack(TemperContent.item("unquenched_weapon"), 1)
        update(result) { it.put("weapon", weapon.save(level.registryAccess())); it.putString("stage", "unquenched") }
        result.set(DataComponents.ITEM_NAME, Component.translatable("item.temperandtang.unquenched_name", weapon.hoverName))
        return heated(result, level)
    }
    fun quenched(stack: ItemStack, level: Level): ItemStack {
        if (blank(stack)) {
            val saved = data(stack).getCompound("weapon")
            val result = ItemStack.parseOptional(level.registryAccess(), saved)
            if (result.isEmpty || result.count != 1 || !WorkshopPlans.basicWeapon(result)) return ItemStack.EMPTY
            stack.get(DataComponents.CUSTOM_NAME)?.let { result.set(DataComponents.CUSTOM_NAME, it.copy()) }
            val foreign = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
            val base = result.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
            foreign.allKeys.filter { it != "temperandtang" }.forEach { base.put(it, foreign.get(it)!!.copy()) }
            if (!base.isEmpty) result.set(DataComponents.CUSTOM_DATA, CustomData.of(base))
            return airCooled(result,level)
        }
        return airCooled(stack, level).also { result -> update(result) { it.putString("stage", "quenched") } }
    }
}
