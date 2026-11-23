package com.bareminimumstudios.temperandtang.smithing

import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.crafting.*

/** Retains the authored pattern while keeping thermal patterns out of ordinary crafting tables. */
class ShapingRecipe(val base: ShapedRecipe) : CraftingRecipe by base {
    override fun getType(): RecipeType<*> = ThermalRecipes.TYPE
    override fun getSerializer(): RecipeSerializer<*> = ThermalRecipes.SERIALIZER
    override fun isSpecial() = true
    override fun getIngredients() = base.ingredients
    override fun getGroup() = base.group
    override fun getRemainingItems(input: CraftingInput) = base.getRemainingItems(input)
}
object ThermalRecipes {
    val TYPE: RecipeType<ShapingRecipe> = object : RecipeType<ShapingRecipe> {
        override fun toString() = "temperandtang:shaping"
    }
    val SERIALIZER: RecipeSerializer<ShapingRecipe> = object : RecipeSerializer<ShapingRecipe> {
        override fun codec(): MapCodec<ShapingRecipe> = RecipeSerializer.SHAPED_RECIPE.codec().xmap(::ShapingRecipe) { it.base }
        override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, ShapingRecipe> = RecipeSerializer.SHAPED_RECIPE.streamCodec().map(::ShapingRecipe) { it.base }
    }
}
