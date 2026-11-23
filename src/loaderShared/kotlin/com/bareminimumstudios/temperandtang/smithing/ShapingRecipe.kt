package com.bareminimumstudios.temperandtang.smithing
import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.crafting.*

class ShapingRecipe(val base:CraftingRecipe):CraftingRecipe by base {
    val width get()=(base as? ShapedRecipe)?.width?:3
    val height get()=(base as? ShapedRecipe)?.height?:((base.ingredients.size+2)/3).coerceAtLeast(1)
    override fun getType():RecipeType<*> = ThermalRecipes.TYPE
    override fun getSerializer():RecipeSerializer<*> = if(base is ShapelessRecipe) ThermalRecipes.SHAPELESS else ThermalRecipes.SERIALIZER
    override fun isSpecial()=true
    override fun getIngredients()=base.ingredients
    override fun getGroup()=base.group
    override fun getRemainingItems(input:CraftingInput)=base.getRemainingItems(input)
}
object ThermalRecipes {
    val TYPE:RecipeType<ShapingRecipe> = object:RecipeType<ShapingRecipe> {override fun toString()="temperandtang:shaping"}
    val SERIALIZER:RecipeSerializer<ShapingRecipe> = object:RecipeSerializer<ShapingRecipe> {
        override fun codec():MapCodec<ShapingRecipe> = RecipeSerializer.SHAPED_RECIPE.codec().xmap(::ShapingRecipe) {it.base as ShapedRecipe}
        override fun streamCodec():StreamCodec<RegistryFriendlyByteBuf,ShapingRecipe> = RecipeSerializer.SHAPED_RECIPE.streamCodec().map(::ShapingRecipe) {it.base as ShapedRecipe}
    }
    val SHAPELESS:RecipeSerializer<ShapingRecipe> = object:RecipeSerializer<ShapingRecipe> {
        override fun codec():MapCodec<ShapingRecipe> = RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(::ShapingRecipe) {it.base as ShapelessRecipe}
        override fun streamCodec():StreamCodec<RegistryFriendlyByteBuf,ShapingRecipe> = RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(::ShapingRecipe) {it.base as ShapelessRecipe}
    }
}
