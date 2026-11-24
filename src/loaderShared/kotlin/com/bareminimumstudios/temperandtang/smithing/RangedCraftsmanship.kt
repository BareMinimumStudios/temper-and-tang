package com.bareminimumstudios.temperandtang.smithing

import com.bareminimumstudios.temperandtang.TemperAndTang
import net.minecraft.world.item.ItemStack

/** Crossbow quality uses projectile power and loading speed, never fictitious melee attributes. */
object RangedCraftsmanship {
    private fun boost(stack:ItemStack,damage:Boolean):Double {
        if(!GearSupport.ranged(stack) || !GearSupport.supported(stack))return 0.0
        val root=Craftsmanship.root(stack);val grade=Craftsmanship.grade(stack)
        val material=CraftMaterials.recorded(stack);val trait=CraftTraits.recorded(stack)
        val finish=Finish.entries.firstOrNull {it.key==root.getString("finish")}
        val precision=if(root.contains("finish_accuracy"))root.getDouble("finish_accuracy").coerceIn(0.0,1.0) else 1.0
        val effect=finish?.let {FinishRules.effect(it,TemperAndTang.config.balance())}
        return ((if(damage)grade?.damage?:0.0 else grade?.speed?:0.0)*TemperAndTang.config.qualityPower.get()+
            (if(damage)material.damage else material.speed)*CraftMaterials.power(stack)+
            (if(damage)trait.damage else trait.speed)*CraftTraits.power(stack)+
            (if(damage)effect?.damage?:0.0 else effect?.speed?:0.0)*(.5+precision*.5)).coerceIn(-.5,1.0)
    }
    fun loadingMultiplier(stack:ItemStack)=1.0+boost(stack,false)
    fun damageMultiplier(stack:ItemStack)=1.0+boost(stack,true)
    fun chargingSeconds(seconds:Float,stack:ItemStack):Float {
        if(!seconds.isFinite() || seconds<=0f)return seconds
        return (seconds/(1.0+boost(stack,false))).toFloat().coerceAtLeast(.05f)
    }
}
