package com.bareminimumstudios.temperandtang

import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeInput

/** Exercise optional native recipes without linking optional mod classes. */
object ForgingVerification {
    fun verify(source: CommandSourceStack) {
        if (!TemperAndTang.modLoaded("overgeared") || !TemperAndTang.config.overgearedRecipes.get()) return
        var iron = 0
        var gold = 0
        var bronze = 0
        var quenching = 0
        for (holder in source.server.recipeManager.recipes) {
            val id = holder.id()
            val ownBronze = id == TemperAndTang.id("bronze_sword")
            val simply = id.namespace == "simplyswords" && TemperAndTang.config.simplySwords.get() && (id.path.startsWith("iron_") || id.path.startsWith("gold_"))
            if (!ownBronze && !simply) continue
            val recipe = holder.value()
            val outputId = BuiltInRegistries.ITEM.getKey(recipe.getResultItem(source.server.registryAccess()).item)
            if (!ownBronze && (outputId.namespace != "simplyswords" || (!outputId.path.startsWith("iron_") && !outputId.path.startsWith("gold_")))) continue
            check(BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.serializer).toString() == "overgeared:forging") { "Not native forging: $id" }
            val cls = recipe.javaClass
            val width = cls.getMethod("getWidth").invoke(recipe) as Int
            val height = cls.getMethod("getHeight").invoke(recipe) as Int
            val nativeIngredients = cls.getMethod("getForgingIngredients").invoke(recipe) as List<*>
            val grid = MutableList(12) { ItemStack.EMPTY }
            for (y in 0 until height) for (x in 0 until width) {
                val native = nativeIngredients[y * width + x]!!
                val ingredient = native.javaClass.getMethod("ingredient").invoke(native) as Ingredient
                if (!ingredient.isEmpty) {
                    check(ingredient.items.isNotEmpty()) { "Empty ingredient in $id" }
                    grid[y * 3 + x] = ingredient.items.first().copy()
                }
            }
            val input = object : RecipeInput {
                override fun getItem(index: Int) = grid[index]
                override fun size() = grid.size
            }
            @Suppress("UNCHECKED_CAST")
            val nativeRecipe = recipe as Recipe<RecipeInput>
            check(nativeRecipe.matches(input, source.level)) { "Unmatchable forging grid: $id" }
            val result = nativeRecipe.assemble(input, source.server.registryAccess())
            check(!result.isEmpty && BuiltInRegistries.ITEM.getKey(result.item) == id) { "Wrong forging output: $id" }
            check(cls.getMethod("getAnvilTier").invoke(recipe) == "stone") { "Unexpected anvil tier: $id" }
            check(cls.getMethod("requiresBlueprint").invoke(recipe) == false) { "Unexpected blueprint requirement: $id" }
            // Existing pack recipes may deliberately require quenching for gold too.
            if (cls.getMethod("needQuenching").invoke(recipe) as Boolean) quenching++
            if (ownBronze) bronze++ else if (id.path.startsWith("iron_")) iron++ else gold++
        }
        check(bronze == 1) { "Missing native bronze forging recipe" }
        if (TemperAndTang.modLoaded("simplyswords") && TemperAndTang.config.simplySwords.get()) check(iron > 0 && gold > 0) { "Missing basic iron/gold forging recipes" }
        TemperAndTang.logger.info("TEMPER_FORGING_VERIFY_PASSED: {} iron, {} gold and {} bronze native recipes matched and assembled; {} require quenching", iron, gold, bronze, quenching)
    }
}
