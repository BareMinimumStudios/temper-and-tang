package com.bareminimumstudios.temperandtang

import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.AbstractCookingRecipe
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.item.crafting.SingleRecipeInput

/** Check registered recipes after tags, unification and pack overrides have been applied. */
object RecipeVerification {
    private fun sample(ingredient: Ingredient): ItemStack {
        if (ingredient.isEmpty) return ItemStack.EMPTY
        check(ingredient.items.isNotEmpty()) { "An ingredient tag has no items" }
        return ingredient.items.first().copy()
    }
    fun verify(source: CommandSourceStack) {
        var crafting = 0
        var cooking = 0
        for (holder in source.server.recipeManager.recipes) {
            if (holder.id().namespace != TemperAndTang.MOD_ID) continue
            val recipe = holder.value()
            if (recipe is CraftingRecipe) {
                val shaped = if (recipe is com.bareminimumstudios.temperandtang.smithing.ShapingRecipe) recipe.base as? ShapedRecipe else recipe as? ShapedRecipe
                val width = shaped?.width ?: 3
                val height = shaped?.height ?: 3
                val items = recipe.ingredients.map(::sample).toMutableList()
                while (items.size < width * height) items.add(ItemStack.EMPTY)
                val input = CraftingInput.of(width, height, items)
                check(recipe.matches(input, source.level)) { "Unmatchable crafting recipe: " + holder.id() }
                check(!recipe.assemble(input, source.server.registryAccess()).isEmpty) { "Empty crafting output: " + holder.id() }
                crafting++
            } else if (recipe is AbstractCookingRecipe) {
                val input = SingleRecipeInput(sample(recipe.ingredients.first()))
                check(recipe.matches(input, source.level)) { "Unmatchable cooking recipe: " + holder.id() }
                check(!recipe.assemble(input, source.server.registryAccess()).isEmpty) { "Empty cooking output: " + holder.id() }
                cooking++
            }
        }
        check(crafting > 0 && cooking > 0) { "Missing survival crafting or cooking recipes" }
        TemperAndTang.logger.info("TEMPER_RECIPES_VERIFY_PASSED: {} crafting and {} cooking recipes matched and assembled", crafting, cooking)
    }
}
