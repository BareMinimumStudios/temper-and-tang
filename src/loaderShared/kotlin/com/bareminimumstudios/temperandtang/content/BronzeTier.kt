package com.bareminimumstudios.temperandtang.content
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Tier
import net.minecraft.world.item.Tiers
import net.minecraft.world.item.crafting.Ingredient

object BronzeTier : Tier by Tiers.IRON {
    override fun getUses() = 220
    override fun getAttackDamageBonus() = 1.0f
    override fun getRepairIngredient(): Ingredient = Ingredient.of(TagKey.create(Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath("c", "ingots/bronze")))
}
