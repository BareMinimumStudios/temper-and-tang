package com.bareminimumstudios.temperandtang.mixin
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.SmithingTransformRecipe
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(SmithingTransformRecipe::class)
interface SmithingIngredientsAccess {
    @Accessor("template") fun temperTemplate():Ingredient
    @Accessor("base") fun temperBase():Ingredient
    @Accessor("addition") fun temperAddition():Ingredient
}
