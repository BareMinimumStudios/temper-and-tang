package com.bareminimumstudios.temperandtang.mixin
import com.bareminimumstudios.temperandtang.smithing.GearTransfer
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import net.minecraft.core.HolderLookup
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.SmithingRecipeInput
import net.minecraft.world.item.crafting.SmithingTransformRecipe
import org.spongepowered.asm.mixin.Mixin

@Mixin(SmithingTransformRecipe::class)
abstract class SmithingUpgradeMixin {
    @WrapMethod(method=["assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup\$Provider;)Lnet/minecraft/world/item/ItemStack;"])
    private fun temperUpgrade(input:SmithingRecipeInput,provider:HolderLookup.Provider,original:Operation<ItemStack>):ItemStack =
        GearTransfer.rebaseUpgrade(original.call(input,provider),input.getItem(1))
}
