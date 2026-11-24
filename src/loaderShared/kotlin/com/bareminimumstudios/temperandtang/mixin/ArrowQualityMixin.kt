package com.bareminimumstudios.temperandtang.mixin

import com.bareminimumstudios.temperandtang.smithing.RangedCraftsmanship
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.projectile.AbstractArrow
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(AbstractArrow::class)
abstract class ArrowQualityMixin {
    @Unique private var temperPowerApplied=false
    @Inject(method=["tick"],at=[At("HEAD")])
    private fun temperPower(ci:CallbackInfo) {
        val arrow=this as Any as AbstractArrow
        if(temperPowerApplied || arrow.level().isClientSide)return
        temperPowerApplied=true
        val weapon=arrow.weaponItem?:return
        val multiplier=RangedCraftsmanship.damageMultiplier(weapon)
        if(multiplier!=1.0 && arrow.baseDamage.isFinite())arrow.baseDamage=(arrow.baseDamage*multiplier).coerceAtLeast(0.0)
    }
    @Inject(method=["addAdditionalSaveData"],at=[At("TAIL")])
    private fun temperSave(tag:CompoundTag,ci:CallbackInfo) {tag.putBoolean("temperandtang_power_applied",temperPowerApplied)}
    @Inject(method=["readAdditionalSaveData"],at=[At("TAIL")])
    private fun temperLoad(tag:CompoundTag,ci:CallbackInfo) {
        // Legacy projectiles already contain their final damage; never multiply a restored arrow again.
        temperPowerApplied=!tag.contains("temperandtang_power_applied") || tag.getBoolean("temperandtang_power_applied")
    }
}
