package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.bareminimumstudios.temperandtang.compat.NativeSmithing
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

object WorkshopPlans {
    private val stock = TagKey.create(Registries.ITEM, TemperAndTang.id("heatable_stock"))
    fun metal(stack: ItemStack) = !stack.isEmpty && (stack.`is`(stock) || NativeSmithing.hot(stack) && !stack.isDamageableItem && !ThermalState.blank(stack))
    fun basicWeapon(stack: ItemStack): Boolean {
        val id = BuiltInRegistries.ITEM.getKey(stack.item)
        return id.toString() in setOf("temperandtang:bronze_sword", "minecraft:iron_sword", "minecraft:golden_sword") ||
            id.namespace == "simplyswords" && TemperAndTang.config.simplySwords.get() && (id.path.startsWith("iron_") || id.path.startsWith("gold_"))
    }
    fun heatable(stack: ItemStack): Boolean {
        if (stack.isEmpty || stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getCompound("temperandtang").contains("finish")) return false
        return metal(stack) || ThermalState.blank(stack) || basicWeapon(stack) && (NativeSmithing.hot(stack) || !ThermalState.data(stack).isEmpty)
    }
    data class Heating(val output: ItemStack, val perItem: Int, val nativeRecipe: RecipeHolder<*>?)
    fun heating(stack: ItemStack, level: Level): Heating? {
        if (!heatable(stack)) return null
        if (NativeSmithing.loaded() && metal(stack) && !NativeSmithing.hot(stack)) {
            val input = SingleRecipeInput(stack.copyWithCount(1))
            val candidate = level.recipeManager.getAllRecipesFor(RecipeType.BLASTING).firstOrNull {
                it.value().matches(input, level) && NativeSmithing.hot(it.value().getResultItem(level.registryAccess()))
            }
            if (candidate != null) {
                val result = candidate.value().assemble(input, level.registryAccess()).copyWithCount(stack.count)
                result.applyComponents(stack.componentsPatch)
                return Heating(result, maxOf(TemperAndTang.config.heatingTicks.get(), candidate.value().cookingTime), candidate)
            }
        }
        return Heating(stack.copy(), TemperAndTang.config.heatingTicks.get(), null)
    }
    class Input(val grid: List<ItemStack>) : RecipeInput {
        override fun getItem(index: Int) = if (index < 9) grid[index] else ItemStack.EMPTY
        override fun size() = 12
    }
    data class Forming(val id: ResourceLocation, val recipe: Recipe<*>, val native: Boolean, val ghosts: List<ItemStack>, val hits: Int, val polishing: Boolean) {
        @Suppress("UNCHECKED_CAST")
        fun matches(grid: List<ItemStack>, level: Level): Boolean = if (native)
            (recipe as Recipe<RecipeInput>).matches(Input(grid), level)
            else (recipe as CraftingRecipe).matches(CraftingInput.of(3, 3, grid), level)
        @Suppress("UNCHECKED_CAST")
        fun assemble(grid: List<ItemStack>, level: Level): ItemStack = if (native)
            (recipe as Recipe<RecipeInput>).assemble(Input(grid), level.registryAccess()) else
            (recipe as CraftingRecipe).assemble(CraftingInput.of(3, 3, grid), level.registryAccess())
        @Suppress("UNCHECKED_CAST")
        fun remainders(grid: List<ItemStack>): List<ItemStack> = if (native)
            (recipe as Recipe<RecipeInput>).getRemainingItems(Input(grid)) else
            (recipe as CraftingRecipe).getRemainingItems(CraftingInput.of(3,3,grid))
        fun current(level: Level) = level.recipeManager.byKey(id).orElse(null)?.value() === recipe
        fun hot(grid: List<ItemStack>, level: Level) = grid.any(WorkshopPlans::metal) && grid.filter(WorkshopPlans::metal).all { ThermalState.hot(it, level) }
        fun required() = maxOf(hits, TemperAndTang.config.shapingStrikes.get())
        fun output(grid: List<ItemStack>, level: Level, player: Player, clean: Int): ItemStack {
            val result = assemble(grid, level)
            if (result.isEmpty) return result
            if (!native) return ThermalState.unquenched(result, level)
            val cls = recipe.javaClass
            if (cls.getMethod("hasQuality").invoke(recipe) == true) {
                var quality = if (clean * 2 >= required()) "WELL" else "POOR"
                val minimum = (cls.getMethod("getMinimumQuality").invoke(recipe) as Enum<*>).name
                if (minimum == "WELL") quality = "WELL"
                if (NativeSmithing.config("INGREDIENTS_DEFINE_MAX_QUALITY") == true && grid.any { NativeSmithing.quality(it) == "POOR" } &&
                    grid.none { NativeSmithing.quality(it) in listOf("WELL", "EXPERT", "PERFECT", "MASTER") }) quality = "POOR"
                NativeSmithing.markForged(result, quality, polishing, player.name.string)
            }
            // The workpiece is physically hot even where the native recipe allows air cooling.
            return ThermalState.heated(result, level)
        }
    }
    fun forming(level: Level): List<Forming> {
        val result = mutableListOf<Forming>()
        if (NativeSmithing.loaded()) {
            if (!NativeSmithing.stoneEnabled()) return result
            @Suppress("UNCHECKED_CAST")
            val type = BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.fromNamespaceAndPath("overgeared", "forging")) as? RecipeType<Recipe<RecipeInput>> ?: return result
            for (holder in level.recipeManager.getAllRecipesFor(type)) {
                val recipe = holder.value()
                val output = recipe.getResultItem(level.registryAccess())
                if (!basicWeapon(output)) continue
                val cls = recipe.javaClass
                if (cls.getMethod("getAnvilTier").invoke(recipe) != "stone" || cls.getMethod("requiresBlueprint").invoke(recipe) == true ||
                    cls.getMethod("needsMinigame").invoke(recipe) == true) continue
                if ((cls.getMethod("getMinimumQuality").invoke(recipe) as Enum<*>).name !in listOf("NONE", "POOR", "WELL") ||
                    (cls.getMethod("getQualityDifficulty").invoke(recipe) as Enum<*>).name != "NONE") continue
                val width = cls.getMethod("getWidth").invoke(recipe) as Int
                val height = cls.getMethod("getHeight").invoke(recipe) as Int
                val ingredients = cls.getMethod("getForgingIngredients").invoke(recipe) as List<*>
                if (ingredients.any { it!!.javaClass.getMethod("transferNBT").invoke(it) == true }) continue
                val simple = ingredients.map { it!!.javaClass.getMethod("ingredient").invoke(it) as Ingredient }
                val ghosts = ghostGrid(simple, width, height) ?: continue
                val hits = cls.getMethod("getHammeringRequired").invoke(recipe) as Int
                if (hits !in 1..40 || ghosts.none(::metal)) continue
                result.add(Forming(holder.id(), recipe, true, ghosts, hits, cls.getMethod("hasPolishing").invoke(recipe) == true))
            }
        } else if (!TemperAndTang.modLoaded("overgeared")) {
            for (holder in level.recipeManager.getAllRecipesFor(ThermalRecipes.TYPE)) {
                val recipe = holder.value()
                if (!basicWeapon(recipe.getResultItem(level.registryAccess()))) continue
                val ghosts = ghostGrid(recipe.ingredients, recipe.base.width, recipe.base.height) ?: continue
                if (ghosts.none(::metal)) continue
                result.add(Forming(holder.id(), recipe, false, ghosts, 6, false))
            }
        }
        return result.sortedBy { it.id.toString() }
    }
    private fun ghostGrid(ingredients: List<Ingredient>, width: Int, height: Int): List<ItemStack>? {
        if (width !in 1..3 || height !in 1..3 || ingredients.any { !it.isEmpty && it.items.isEmpty() }) return null
        val grid = MutableList(9) { ItemStack.EMPTY }
        val offset = (3-width)/2
        for (y in 0 until height) for (x in 0 until width) {
            val ingredient = ingredients[y*width+x]
            if (!ingredient.isEmpty) grid[y*3+x+offset] = ingredient.items.first().copyWithCount(1)
        }
        return grid
    }
}
