package com.bareminimumstudios.temperandtang.station

import com.bareminimumstudios.temperandtang.TemperAndTang
import com.pockyl.diegesis.api.station.StationContext
import net.minecraft.nbt.CompoundTag

object SmithingFeedback {
    fun strike(station:StationContext,node:String,accuracy:Double) {
        if(station.isClientSide)return
        station.effect(TemperAndTang.id("hammer_strike"),node,CompoundTag().also {
            it.putFloat("accuracy",accuracy.toFloat())
            it.putFloat("pitch",.96f+station.level().random.nextFloat()*.12f)
            it.putBoolean("sparks",TemperAndTang.config.sparks.get())
        })
    }
}
