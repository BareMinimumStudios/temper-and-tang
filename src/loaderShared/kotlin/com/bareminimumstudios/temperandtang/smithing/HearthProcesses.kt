package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level

object HearthProcesses {
    private val alloyPriority = compareBy<RecipeHolder<AlloyRecipe>>(
        { if (it.id().namespace == TemperAndTang.MOD_ID) 0 else 1 },
        { it.id().toString() }
    )

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
    fun alloy(primary: ItemStack, additive: ItemStack, level: Level): WorkshopPlans.Heating? {
        if (!WorkshopPlans.stock(primary) || additive.isEmpty) return null
        val input = AlloyInput(primary, additive)
        var selected: RecipeHolder<AlloyRecipe>? = null
        var selectedBatch: AlloyBatch? = null
        for (holder in level.recipeManager.getAllRecipesFor(AlloyRecipes.TYPE)) {
            if (selected != null && alloyPriority.compare(holder, selected) >= 0) continue
            val recipe = holder.value()
            if (!recipe.matches(input, level) || !WorkshopPlans.stock(recipe.result)) continue
            val batch = AlloyRules.batch(
                primary.count, additive.count, recipe.primaryCount, recipe.additiveCount,
                recipe.result.count, minOf(8, recipe.result.maxStackSize)
            ) ?: continue
            selected = holder
            selectedBatch = batch
        }

        val holder = selected ?: return null
        val batch = checkNotNull(selectedBatch)
        val recipe = holder.value()
        return WorkshopPlans.Heating(
            recipe.result.copyWithCount(batch.outputCount), recipe.cookingTime, holder,
            batch.additiveCost, "alloy", (recipe.cookingTime.toLong() * batch.batches).coerceAtMost(96000).toInt(),
            recipe.requiresAdvanced
        )
    }
}
