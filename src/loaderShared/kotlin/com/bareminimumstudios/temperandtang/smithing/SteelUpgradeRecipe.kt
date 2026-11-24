package com.bareminimumstudios.temperandtang.smithing

import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.crafting.*
import net.minecraft.world.level.Level

/** Native weapon identity, but a steel-forged core is mandatory before diamond reinforcement. */
class SteelUpgradeRecipe(val base:SmithingTransformRecipe):SmithingRecipe by base {
    override fun matches(input:SmithingRecipeInput,level:Level)=base.matches(input,level) &&
        (CraftMaterials.recorded(input.getItem(1))==ForgeMaterial.STEEL || CraftMaterials.identify(input.getItem(1))==ForgeMaterial.STEEL)
    override fun getSerializer():RecipeSerializer<*> = SteelUpgrades.SERIALIZER
}
object SteelUpgrades {
    val SERIALIZER:RecipeSerializer<SteelUpgradeRecipe> = object:RecipeSerializer<SteelUpgradeRecipe> {
        override fun codec():MapCodec<SteelUpgradeRecipe> = RecipeSerializer.SMITHING_TRANSFORM.codec().xmap(::SteelUpgradeRecipe) {it.base}
        override fun streamCodec():StreamCodec<RegistryFriendlyByteBuf,SteelUpgradeRecipe> = RecipeSerializer.SMITHING_TRANSFORM.streamCodec().map(::SteelUpgradeRecipe) {it.base}
    }
}
