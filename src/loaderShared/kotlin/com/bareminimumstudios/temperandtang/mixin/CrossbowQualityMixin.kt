package com.bareminimumstudios.temperandtang.mixin

import com.bareminimumstudios.temperandtang.smithing.RangedCraftsmanship
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.enchantment.EnchantmentHelper
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At

@Mixin(EnchantmentHelper::class)
abstract class CrossbowQualityMixin {
    private companion object {
        @JvmStatic
        @ModifyReturnValue(method=["modifyCrossbowChargingTime"],at=[At("RETURN")])
        private fun temperLoading(seconds:Float,stack:ItemStack,owner:LivingEntity):Float =
            RangedCraftsmanship.chargingSeconds(seconds,stack)
    }
}

