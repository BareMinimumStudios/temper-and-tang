package com.bareminimumstudios.temperandtang.smithing

import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity

/** Loader hooks honor ordinary furnace fuels; alloy carbon remains a separate recipe ingredient. */
object WorkshopFuel {
    var burnTime:(ItemStack)->Int={AbstractFurnaceBlockEntity.getFuel()[it.item]?:0}
    var remainder:(ItemStack)->ItemStack={if(it.item.hasCraftingRemainingItem())ItemStack(it.item.craftingRemainingItem!!)else ItemStack.EMPTY}
    fun accepts(stack:ItemStack)=!stack.isEmpty && burnTime(stack)>0
    fun cost(stack:ItemStack,ticks:Int):Int {val burn=burnTime(stack);return if(burn<=0)0 else ((ticks.toLong()+burn-1)/burn).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()}
    fun sufficient(stack:ItemStack,ticks:Int)=accepts(stack) && stack.count>=cost(stack,ticks)
    fun spend(stack:ItemStack,ticks:Int):ItemStack {
        require(sufficient(stack,ticks))
        val count=cost(stack,ticks);val rest=remainder(stack)
        // Container fuels are nonstackable in ordinary gameplay. Reject ambiguous custom stacks rather than lose containers.
        require(rest.isEmpty || stack.count==1 && count==1)
        return if(rest.isEmpty)stack.copy().also {it.shrink(count)} else rest
    }
}
