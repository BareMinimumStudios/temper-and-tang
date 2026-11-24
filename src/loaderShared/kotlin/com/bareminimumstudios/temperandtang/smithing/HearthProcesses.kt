package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level

object HearthProcesses {
    val REFINABLE=TagKey.create(Registries.ITEM,TemperAndTang.id("refinable_stock"))
    fun refinable(stack:ItemStack)=!stack.isEmpty && stack.`is`(REFINABLE)
    fun duration(plan:WorkshopPlans.Heating,count:Int)=if(plan.totalTicks>0)plan.totalTicks else ThermalRules.heatingTicks(plan.perItem,count)
    fun refine(stack:ItemStack,level:Level):WorkshopPlans.Heating? {
        if(!refinable(stack))return null
        val input=SingleRecipeInput(stack.copyWithCount(1))
        val holder=level.recipeManager.getRecipeFor(RecipeType.SMELTING,input,level).orElse(null)?:return null
        val result=holder.value().assemble(input,level.registryAccess())
        if(!WorkshopPlans.stock(result) || result.count.toLong()*stack.count>minOf(8,result.maxStackSize))return null
        return WorkshopPlans.Heating(result.copyWithCount(result.count*stack.count),holder.value().cookingTime,holder,process="refine")
    }
    fun alloy(primary:ItemStack,additive:ItemStack,level:Level):WorkshopPlans.Heating? {
        if(!WorkshopPlans.stock(primary))return null
        val input=AlloyInput(primary,additive)
        for(holder in level.recipeManager.getAllRecipesFor(AlloyRecipes.TYPE).sortedWith(compareBy({if(it.id().namespace==TemperAndTang.MOD_ID)0 else 1},{it.id().toString()}))) {
            val r=holder.value()
            if(!r.matches(input,level) || !WorkshopPlans.stock(r.result))continue
            val batch=AlloyRules.batch(primary.count,additive.count,r.primaryCount,r.additiveCount,r.result.count,minOf(8,r.result.maxStackSize))?:continue
            return WorkshopPlans.Heating(r.result.copyWithCount(batch.outputCount),r.cookingTime,holder,batch.additiveCost,"alloy",(r.cookingTime.toLong()*batch.batches).coerceAtMost(96000).toInt(),r.requiresAdvanced)
        }
        return null
    }
}
